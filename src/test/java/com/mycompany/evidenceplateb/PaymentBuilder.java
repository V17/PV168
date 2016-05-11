/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import com.mycompany.evidenceplateb.Entity;
import com.mycompany.evidenceplateb.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

/**
 *
 * @author vozka
 */
public class PaymentBuilder {
    private Long id;
    private BigDecimal sum;
    private LocalDate date;
    private Entity from;
    private Entity to;
    
    public PaymentBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public PaymentBuilder sum(BigDecimal sum) {
        this.sum = sum;
        return this;
    }
    
    public PaymentBuilder from(Entity from) {
        this.from = from;
        return this;
    }
    
    public PaymentBuilder to(Entity to) {
        this.to = to;
        return this;
    }
    
    public PaymentBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }
    
    public PaymentBuilder date(int year, Month month, int day) {
        this.date = LocalDate.of(year, month, day);
        return this;
    }
    
    public Payment build() {
        Payment payment;
        payment = new Payment();
        payment.setId(id);
        payment.setSum(sum);
        payment.setDate(date);
        payment.setFrom(from);
        payment.setTo(to);
        return payment; 
    }
}
