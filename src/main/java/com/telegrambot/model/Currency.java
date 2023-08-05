package com.telegrambot.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Currency {
    private String txt;
    private BigDecimal rate;
    private String cc;
    private String exchangedate;


}
