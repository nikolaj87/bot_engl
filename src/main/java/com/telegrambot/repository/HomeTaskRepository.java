package com.telegrambot.repository;

import com.telegrambot.entity.HomeTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeTaskRepository extends JpaRepository<HomeTask, Long> {

}
