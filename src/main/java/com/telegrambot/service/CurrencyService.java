package com.telegrambot.service;

import com.telegrambot.bot.TelegramBot;
import com.telegrambot.model.CurrencyModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class CurrencyService {


    public static String getCurrencyRate(String message, CurrencyModel model) throws IOException, ParseException {

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDateForUser = currentDate.format(dateFormatter);

        URL url = new URL("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchangenew?valcode="
                + message + "&date=" + formattedDate + "&json");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.nextLine());
        }
        JSONArray jsonArray = new JSONArray(result.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);

            model.setTxt(object.getString("txt"));
            model.setRate(object.getDouble("rate"));
            model.setCc(object.getString("cc"));
            model.setExchangedate(object.getString("exchangedate"));
        }
        //новое
        if (jsonArray.isEmpty()){
            return "We have not found such a currency." + "\n" +
                    "Enter the currency whose official exchange rate" + "\n" +
                    "you want to know in relation to HRN." + "\n" +
                    "For example: USD $,EUR €,GBP £,KZT ₸,AZN ₼,TRY ₺,PLN zł and many other world currencies";
        }
        //конец
        return "Official exchange rate of " + model.getCc() + " is " + model.getRate() + " HRN" + " as of " + formattedDateForUser;
    }




}
