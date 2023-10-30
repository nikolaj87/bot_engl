package com.telegrambot.repository;

import com.telegrambot.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HomeTaskRepository extends JpaRepository<Homework, Long> {
    Optional<Homework> findHomeTaskById(Long studentId);
    @Modifying
    @Query(value = "UPDATE home_task SET is_active = 0 WHERE id = :id", nativeQuery = true)
    void updateHomeTaskByIdSetIsActiveFalse (long id);
}
