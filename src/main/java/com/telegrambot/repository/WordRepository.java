package com.telegrambot.repository;

import com.telegrambot.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WordRepository extends JpaRepository<Word, Long> {

    @Query(value = "SELECT word_english FROM word WHERE word_original = :originalWord", nativeQuery = true)
    String getEnglishByOriginal (String originalWord);
    @Query(value = "SELECT word_original FROM word WHERE student_id = :studentId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    String getAnyEnglishWordByStudentId(Long studentId);
//    Word getFirstByStudentId(Long studentId);
}
