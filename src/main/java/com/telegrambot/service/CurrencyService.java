package com.telegrambot.service;

import com.telegrambot.model.Currency;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@Service
public class CurrencyService {

    private static final int NUMBER_INDEX = 0;
    private static final int CODE_INDEX = 1;

    private static final String START_BUTTON = "/start";
    private static final String ANSWER = " ,hi, nice to meet you!" + "\n" +
            "Enter the currency whose official exchange rate" + "\n" +
            "you want to know in relation to HRN." + "\n" +
            "For example: 100 USD";

    private static final String NO_SUCH_CURRENCY_MESSAGE = "We have not found such a message." + "\n" +
            "Enter the message whose official exchange rate" + "\n" +
            "you want to know in relation to HRN." + "\n" +
            "For example: USD $,EUR €,GBP £,KZT ₸,AZN ₼,TRY ₺,PLN zł and many other world currencies";

    public  String getCurrencyRate(String message) throws IOException {
        Currency currency = getCurrency(message);
        String formattedDateForUser = getDateForUser();

        if (currency.getCc().isEmpty()) {
            return NO_SUCH_CURRENCY_MESSAGE;
        }

        return "Official exchange rate of " + currency.getCc() + " is " + currency.getRate() +
                " HRN\uD83C\uDDFA\uD83C\uDDE6"
                + " as of:  " + "Date: " +
                formattedDateForUser;
    }

    public Currency getCurrency(String currencyCode) throws IOException{
        Currency currency = new Currency();

        String formattedDate = getDateForURL();

        URL url = new URL("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchangenew?valcode="
                + currencyCode + "&date=" + formattedDate + "&json");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.nextLine());
        }
        JSONArray jsonArray = new JSONArray(result.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);

            currency.setTxt(object.getString("txt"));
            currency.setRate(BigDecimal.valueOf(object.getDouble("rate")));
            currency.setCc(object.getString("cc"));
            currency.setExchangedate(object.getString("exchangedate"));
        }

       return currency;
    }

    private static String getDateForURL() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return currentDate.format(formatter);
    }

    private static String getDateForUser() {
        LocalDate currentTime = LocalDate.now();  //todo сделать зону для Украины
//        int hour = currentTime.getHour();
//        int minute = currentTime.getMinute();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String result = currentTime.format(dateFormatter);

        return result;

    }


    public SendMessage handleUpdate(Update update) {

        long chatId = update.getMessage().getChatId();
        String message = "";

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();


            if (messageText.equals(START_BUTTON)) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            }
            if (messageText.matches("[A-Za-z]{3}")) {
                try {
                    message = getCurrencyRate(messageText);
                } catch (IOException e) {
                    sendMessage(chatId, NO_SUCH_CURRENCY_MESSAGE);
                }

            }
            if (messageText.matches("\\d+[:\\s-][A-Za-z]{3}")){
                try {
                    message = calculateCurrency(messageText);
                } catch (IOException e) {
                    sendMessage(chatId, NO_SUCH_CURRENCY_MESSAGE);
                }
            }
        }
        return sendMessage(chatId, message);
    }

    private String calculateCurrency(String messageText) throws IOException{
        String formattedDateForUser = getDateForUser();
        String[] numberCodeArray = messageText.split("[:\\s-]");
        int numberOfCurrency = Integer.parseInt(numberCodeArray[NUMBER_INDEX]);
        String currencyCode = numberCodeArray[CODE_INDEX];
        Currency currency = getCurrency(currencyCode);
        return "Official converted exchange rate of " + numberOfCurrency+ " "+ currency.getCc() + " is " +
                currency.getRate().multiply(BigDecimal.valueOf(numberOfCurrency))  + " HRN\uD83C\uDDFA\uD83C\uDDE6"
                + " as of " + "Date: " +
                formattedDateForUser;
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = name + ANSWER;
        sendMessage(chatId, answer);
    }

    private SendMessage sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        return sendMessage;
    }
}
