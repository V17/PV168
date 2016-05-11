/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import java.util.List;

/**
 *
 * @author User
 */
public interface PaymentManager {
    
    void createPayment(Payment payment);
    
    void updatePayment(Payment payment);
    
    void deletePayment(Payment payment);
    
    Payment findPaymentById(Long id);
    
    Payment findPaymentByTo(Entity entity);
     
    Payment findPaymentByFrom(Entity entity);
    
    List<Payment> findAllPayments();
}
