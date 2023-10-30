package com.telegrambot.cache;

import com.telegrambot.entity.Word;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CacheList {
    Map<Long, List<Word>> actualStudentWords = new HashMap<>();

    public Word putAndReturnAny(long studentId, List<Word> words) {
        Collections.shuffle(words);
        Word anyWord = words.get(0);
        words.remove(anyWord);
        actualStudentWords.put(studentId, words);
        return anyWord;
    }

    public boolean isEmpty(long studentId) {
        return actualStudentWords.get(studentId).isEmpty();
    }

    public Word getAndDelete(long studentId) {
        List<Word> wordList = actualStudentWords.get(studentId);
        Word anyWord = wordList.get(0);
        wordList.remove(anyWord);
        return anyWord;
    }

    public void remove(long studentId) {
        actualStudentWords.remove(studentId);
    }
}
