package com.telegrambot.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Cache {
    private final Map<Long, String> cache = new ConcurrentHashMap<>();

    public void put(Long studentId, String word) {
        cache.put(studentId, word);
    }
    public boolean cacheCheck (long studentId) {
        return cache.containsKey(studentId);
    }
    public void cacheEvict () {
        cache.clear();
    }
    public void remove (long studentId) {
        cache.remove(studentId);
    }
    public String get (long studentId) {
        return cache.get(studentId);
    }
}
