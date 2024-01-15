package com.telegrambot.entity;

import jakarta.persistence.*;


import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "student")
public class Student {
    @Id
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "registered_at")
    private Timestamp registeredAt;

    public Student(long id, String name, Timestamp registeredAt) {
        this.id = id;
        this.name = name;
        this.registeredAt = registeredAt;
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

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
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
                ", registeredAt=" + registeredAt +
                '}';
    }
}
