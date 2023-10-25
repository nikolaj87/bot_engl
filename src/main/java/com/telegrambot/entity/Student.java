package com.telegrambot.entity;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "student")
public class Student {
    @Id
    private long id;
    @Column(name = "name")
    private String name;
//    @OneToMany
//    private Set<Word> words;
//    @OneToOne
//    private HomeTask homeTask;

    public Student(long id, String name, Set<Word> words, HomeTask homeTask) {
        this.id = id;
        this.name = name;
//        this.words = words;
//        this.homeTask = homeTask;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public Student() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
