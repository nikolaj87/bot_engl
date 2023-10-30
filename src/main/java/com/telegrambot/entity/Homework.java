package com.telegrambot.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "home_task")
public class Homework {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
//    @Column(name = "student_id")
//    private long studentId;
    @Column(name = "description")
    private String description;
    @Column(name = "is_active")
    private int isActive;

    public Homework(long id, String description, int isActive) {
        this.id = id;
        this.description = description;
        this.isActive = isActive;
    }

    public Homework() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }




    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int isActive() {
        return isActive;
    }

    public void setActive(int active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Homework homework = (Homework) o;
        return id == homework.id && isActive == homework.isActive && Objects.equals(description, homework.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, isActive);
    }

    @Override
    public String toString() {
        return "HomeTask{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
