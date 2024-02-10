package com.telegrambot.service;

import com.telegrambot.dto.ChatGPTRequest;
import com.telegrambot.dto.ChatGPTResponse;
import com.telegrambot.entity.Word;
import com.telegrambot.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

@Service
public class GptServiceImpl implements GptService {

    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private ServiceImpl service;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.api.url}")
    private String url;
    @Value("${admin_id}")
    private long adminId;

    @Override
    public List<SendMessage> createContext(long chatId, String userText) {
        String messageForChat = createMessageForChat(userText.substring(3));
        ChatGPTRequest request = new ChatGPTRequest(model, messageForChat);
        ChatGPTResponse response = restTemplate.postForObject(url, request, ChatGPTResponse.class);
        String content = response.getChoices().get(0).getMessage().getContent();
        return List.of(new SendMessage(String.valueOf(chatId), content));
    }

    @Override
    public List<SendMessage> createFillTheGaps(String messageText) {
        String number = messageText.substring(4).trim();
        int numberLong = Integer.parseInt(number);
        long currentStudentId = service.getCurrentStudentId();
        List<Word> lastWords = wordRepository.getLastWords(currentStudentId, numberLong);
        Collections.shuffle(lastWords);
        String messageForGPT = createMessageFillTheGaps(lastWords);
        ChatGPTRequest request = new ChatGPTRequest(model, messageForGPT);
        ChatGPTResponse response = restTemplate.postForObject(url, request, ChatGPTResponse.class);
        String studentContent = response.getChoices().get(0).getMessage().getContent();
        return List.of(new SendMessage(String.valueOf(adminId), studentContent));
    }

    private String createMessageFillTheGaps(List<Word> lastWords) {
        StringBuilder words = new StringBuilder();
        int size = lastWords.size();
        for (Word word : lastWords) {
            words.append(word.getWordEnglish())
                    .append(", ");
        }
        return
                "Я даю тебе " + size + " слов на английском языке - " + words + ". " +
                        "Tвоя задача придумать " + size + " предложений на английском языке с использованием " +
                        "этих слов, но сами слова нужно заменить вот таким прочерком - _______. 1 слово - 1 предложение." +
                        " Длина предложений 6 - 12 слов. Уровень английского A2 - B1. " +
                        "Дай мне только эти " + size + " предложения без комментариев";
    }

    private String createMessageForChat(String userText) {
        return "Сейчас я дам тебе слово - " + userText + ". Твоя задача создать 2 предложения" +
                " на основании этого слова, которые покажут контекст его использования." +
                " Первое предложение на английском языке из 7 - 13 слов. Второе - " +
                "перевод этого предложения на русский язык. Не вставляй никаких " +
                "комментариев. Раздели эти 2 предложения 2 переносами строки." +
                " Используй лексику B1 для английского языка.";
    }
}
