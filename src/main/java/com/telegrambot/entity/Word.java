package com.telegrambot.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "word")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "word_english")
    private String wordEnglish;
    @Column(name = "word_original")
    private String wordOriginal;
    @Column(name = "student_id")
    private long studentId;

    public Word(long id, String wordEnglish, String wordOriginal, long studentId) {
        this.id = id;
        this.wordEnglish = wordEnglish;
        this.wordOriginal = wordOriginal;
        this.studentId = studentId;
    }

    public Word() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWordEnglish() {
        return wordEnglish;
    }

    public void setWordEnglish(String wordEnglish) {
        this.wordEnglish = wordEnglish;
    }

    public String getWordOriginal() {
        return wordOriginal;
    }

    public void setWordOriginal(String wordOriginal) {
        this.wordOriginal = wordOriginal;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", wordEnglish='" + wordEnglish + '\'' +
                ", wordOriginal='" + wordOriginal + '\'' +
                ", studentId=" + studentId +
                '}';
    }
}
