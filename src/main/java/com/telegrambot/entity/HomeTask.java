package com.telegrambot.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "home_task")
public class HomeTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "student_id")
    private long student_id;
    @Column(name = "description")
    private String description;
    @Column(name = "is_active")
    private boolean isActive;

    public HomeTask(long id, long student_id, String description, boolean isActive) {
        this.id = id;
        this.student_id = student_id;
        this.description = description;
        this.isActive = isActive;
    }

    public HomeTask() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStudent_id() {
        return student_id;
    }

    public void setStudent_id(long student_id) {
        this.student_id = student_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeTask homeTask = (HomeTask) o;
        return id == homeTask.id && student_id == homeTask.student_id && isActive == homeTask.isActive && Objects.equals(description, homeTask.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, student_id, description, isActive);
    }

    @Override
    public String toString() {
        return "HomeTask{" +
                "id=" + id +
                ", student_id=" + student_id +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
