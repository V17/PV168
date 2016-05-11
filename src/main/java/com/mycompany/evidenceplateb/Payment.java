/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author User
 */
public class Payment {
    
    private Long id;
    private BigDecimal sum;
    private LocalDate date;
    private Entity from;
    private Entity to;

    public Payment() {
        
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public LocalDate getDate() {
        return date;
    }

    public Entity getFrom() {
        return from;
    }

    public Entity getTo() {
        return to;
    } 

    public void setId(Long id) {
        this.id = id;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setFrom(Entity from) {
        this.from = from;
    }

    public void setTo(Entity to) {
        this.to = to;
    }
    
    @Override
    public String toString() {
        return "Payment:" + id + " ," + sum + " ," + date + " ," + from + " ," + to;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Payment other = (Payment) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
