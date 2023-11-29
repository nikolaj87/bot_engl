package com.telegrambot.repository;

import com.telegrambot.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    @Query(value = "SELECT word_english FROM word WHERE word_original = :originalWord", nativeQuery = true)
    String getEnglishByOriginal (String originalWord);
    @Query(value = "SELECT word_original FROM word WHERE student_id = :studentId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    String getAnyEnglishWordByStudentId_21day(Long studentId);
    @Query(value = "SELECT * FROM word WHERE student_id = :studentId AND DATE_SUB(NOW(), INTERVAL 336 HOUR) < created_at ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Word> getAnyWordByStudentId(Long studentId);


//    @Query("SELECT w FROM Word w WHERE w.studentId = :studentId AND w.createdAt > DATE_ADD(NOW(), 336, 'HOUR')")
//    @Query("SELECT word FROM Word word WHERE word.studentId = :studentId AND word.createdAt > DATE_ADD(NOW(), 336, 'HOUR')")
//    SELECT * FROM word WHERE student_id = 795942078 AND DATE_SUB(NOW(), INTERVAL 336 HOUR) < created_at;

//    @Query(value = "SELECT * FROM word WHERE student_id = :studentId AND DATE_SUB(NOW(), INTERVAL 336 HOUR) < created_at", nativeQuery = true)
    @Query(value = "SELECT * FROM word WHERE student_id = :studentId AND DATE_SUB(NOW(), INTERVAL 336 HOUR) < created_at", nativeQuery = true)
    List<Word> getAllNewWords(long studentId);

    @Query(value = "SELECT * FROM word WHERE student_id = :studentId AND is_archive = 0", nativeQuery = true)
    List<Word> getAllStudentWords(long studentId);

    @Query(value = "SELECT * FROM word WHERE student_id = :studentId AND is_archive = 1", nativeQuery = true)
    List<Word> getArchiveStudentWords(long studentId);

    @Modifying
    @Query(value = "UPDATE word SET is_archive = '1' WHERE student_id = :studentId AND word_english = :word", nativeQuery = true)
    void wordToArchive (long studentId, String word);

    @Modifying
    @Query(value = "UPDATE word SET is_archive = '0' WHERE student_id = :studentId AND word_english = :word", nativeQuery = true)
    void wordToList (long studentId, String word);

    @Query(value = "SELECT * FROM (SELECT * FROM word WHERE student_id = :studentId ORDER BY created_at DESC LIMIT 30) AS last30 ORDER BY created_at ASC", nativeQuery = true)
    List<Word> getLast30Words(long studentId);


    //    List<Word> getAllByStudentIdAndAndCreatedAt (long studentId, Timestamp timestamp);
}
