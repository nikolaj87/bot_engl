package com.telegrambot.service;

import com.telegrambot.entity.Student;
import com.telegrambot.repository.StudentRepository;
import com.telegrambot.utils.KeyboardGenerator;
import com.telegrambot.utils.MessageGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@org.springframework.stereotype.Service
public class AdminServiceImpl implements AdminService {

    private final StudentRepository studentRepository;
    private final KeyboardGenerator keyboards;
    private final MessageGenerator generator;
    private final ServiceImpl service;

    @Value("${admin_id}")
    private long adminId;

    public AdminServiceImpl(StudentRepository studentRepository, KeyboardGenerator keyboards, MessageGenerator generator, Service service, ServiceImpl service1) {
        this.studentRepository = studentRepository;
        this.keyboards = keyboards;
        this.generator = generator;
        this.service = service1;
    }

    @Override
    public List<SendMessage> adminCommand() {
        List<Student> allStudents = studentRepository.findAll();
        return keyboards.generateStudentList(allStudents, adminId);
    }

    @Override
    public List<SendMessage> switchStudent(String text) {
        String studentId = text.substring(7, text.indexOf(" "));
        String studentName = text.substring(text.indexOf(" ")).toUpperCase();
        service.setCurrentStudentId(Long.parseLong(studentId));
        SendMessage adminMessage = new SendMessage(String.valueOf(adminId), "переключено на студента" + studentName +
                "\nТеперь он получает прямые сообщения и можно сохранить для него слова и домашку");
        return List.of(adminMessage);
    }
}

