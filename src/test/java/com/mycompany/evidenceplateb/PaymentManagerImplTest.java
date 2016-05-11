/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;


import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.IllegalPaymentException;
import cz.muni.fi.pv168.common.ValidationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static java.time.Month.DECEMBER;
import java.util.Comparator;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 *
 * @author User
 */
public class PaymentManagerImplTest {
    
    
    private DataSource dataSource;
    private PaymentManagerImpl paymentManager;
    private EntityManagerImpl entityManager;
    
    private Entity entity1, entity2;
    
    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        paymentManager = new PaymentManagerImpl();
        entityManager = new EntityManagerImpl();
        DBUtils.executeSqlScript(dataSource,PaymentManager.class.getResource("createTables.sql"));
        paymentManager.setDataSource(dataSource); 
        entityManager.setDataSource(dataSource);
        setUpData();
    }
    
    private void setUpData() {
        entity1 = sample1EntityBuilder().build();
        entity2 =  sample2EntityBuilder().build();        
        entityManager.createEntity(entity1);
        entityManager.createEntity(entity2);
        
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE ENTITY").executeUpdate();
            connection.prepareStatement("DROP TABLE PAYMENT").executeUpdate();
        }
    }
    
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:paymentmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    private PaymentBuilder sample1PaymentBuilder() {
        Entity e1 = sample1EntityBuilder().build();
        Entity e2 =  sample2EntityBuilder().build();
        return new PaymentBuilder()
                .sum(BigDecimal.ONE)
                .date(2106,DECEMBER,9)
                .from(entity2)
                .to(entity1);
    }
    
    private PaymentBuilder sample2PaymentBuilder() {
        Entity e1 = sample1EntityBuilder().build();
        Entity e2 =  sample2EntityBuilder().build();
        return new PaymentBuilder()
                .sum(BigDecimal.TEN)
                .date(2016,DECEMBER,10)
                .from(entity1)
                .to(entity2);
    }
    
    private EntityBuilder sample1EntityBuilder(){
        return new EntityBuilder()
                .name("A");
    }
    
    private EntityBuilder sample2EntityBuilder(){
        return new EntityBuilder()
                .name("B");
    }
    
    @Test
    public void createPayment() {
        Payment payment = sample1PaymentBuilder().build();
        
        paymentManager.createPayment(payment);

        Long paymentId = payment.getId();
        assertThat(paymentId).isNotNull();

        assertThat(paymentManager.findPaymentById(paymentId))
                .isNotSameAs(payment)
                .isEqualToComparingFieldByField(payment);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        paymentManager.createPayment(null);
    }
    
    @Test
    public void createPaymentWithExistingId() {
        Payment payment = sample1PaymentBuilder().id(1L).build();
        expectedException.expect(IllegalArgumentException.class);
        paymentManager.createPayment(payment);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createPaymentWithNullFrom() {
        Payment payment = sample1PaymentBuilder().id(2L).from(null).build();
        paymentManager.createPayment(payment);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createPaymentWithNullTo() {
        Payment payment = sample1PaymentBuilder().id(2L).to(null).build();
        paymentManager.createPayment(payment);
        Payment result = paymentManager.findPaymentById(payment.getId());
        assertThatThrownBy(() -> paymentManager.createPayment(payment))
                .isInstanceOf(ValidationException.class);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void createPaymentWithNullDate() {
        Payment payment = sample1PaymentBuilder().id(2L).date(null).build();
        paymentManager.createPayment(payment);
    }
    
    @Test (expected = IllegalStateException.class)
    public void createPaymentWithNullSum() {
        Payment payment = sample1PaymentBuilder().id(2L).sum(null).build();
        paymentManager.createPayment(payment);
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
        
    private void testUpdatePayment(Operation<Payment> updateOperation) {
        Payment paymentForUpdate = sample1PaymentBuilder().build();
        Payment anotherPayment = sample2PaymentBuilder().build();
        paymentManager.createPayment(paymentForUpdate);
        paymentManager.createPayment(anotherPayment);

        updateOperation.callOn(paymentForUpdate);

        paymentManager.updatePayment(paymentForUpdate);
        assertThat(paymentManager.findPaymentById(paymentForUpdate.getId()))
                .isEqualToComparingFieldByField(paymentForUpdate);
        
        assertThat(paymentManager.findPaymentById(anotherPayment.getId()))
                .isEqualToComparingFieldByField(anotherPayment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updatePaymentDateNull() {
        testUpdatePayment((payment) -> payment.setDate(null));
    }
    
    @Test(expected = IllegalStateException.class)
    public void updatePaymentSumNull() {
        testUpdatePayment((payment) -> payment.setSum(null));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updatePaymentFromNull() {
        testUpdatePayment((payment) -> payment.setFrom(null));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updatePaymentToNull() {
        testUpdatePayment((payment) -> payment.setTo(null));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void updateNullPayment() {
        paymentManager.updatePayment(null);
    }

    @Test
    public void updatePaymentWithNullId() {
        Payment payment = sample1PaymentBuilder().id(null).build();
        expectedException.expect(IllegalPaymentException.class);
        paymentManager.updatePayment(payment);
    }

    @Test
    public void updateNonExistingPayment() {
        Payment payment = sample1PaymentBuilder().id(null).build();
        expectedException.expect(IllegalPaymentException.class);
        paymentManager.updatePayment(payment);
    }
    
    @Test
    public void findAllPayments() {

        assertThat(paymentManager.findAllPayments().isEmpty());

        Payment s1 = sample1PaymentBuilder().build();
        Payment s2 = sample2PaymentBuilder().build();

        paymentManager.createPayment(s1);
        paymentManager.createPayment(s2);

        assertThat(paymentManager.findAllPayments())
                .usingFieldByFieldElementComparator()
                .containsOnly(s1,s2);
    
    }  
       
    @Test
    public void findPaymentById() {
        
        Payment s1 = sample1PaymentBuilder().build();

        paymentManager.createPayment(s1);

        assertThat(paymentManager.findPaymentById(s1.getId()))
                .isEqualToComparingFieldByField(s1);
    }
    
    @Test
    public void findPaymentByTo() {

        Payment s1 = sample1PaymentBuilder().build();
        
        assertThat(paymentManager.findPaymentByTo(s1.getTo())).isNull();


        paymentManager.createPayment(s1);

        assertThat(paymentManager.findPaymentByTo(s1.getTo()))
                .isEqualToComparingFieldByField(s1);
    }
    
    @Test
    public void findPaymentByFrom() {
        
        Payment s1 = sample1PaymentBuilder().build();
        assertThat(paymentManager.findPaymentByFrom(s1.getFrom())).isNull();

        paymentManager.createPayment(s1);

        assertThat(paymentManager.findPaymentByFrom(s1.getFrom()))
                .isEqualToComparingFieldByField(s1);
    }
    
    @Test
    public void deletePayment() {
        Payment s1 = sample1PaymentBuilder().build();
        Payment s2 = sample2PaymentBuilder().build();
        paymentManager.createPayment(s1);
        paymentManager.createPayment(s2);

        assertThat(paymentManager.findPaymentById(s1.getId())).isNotNull();
        assertThat(paymentManager.findPaymentById(s2.getId())).isNotNull();

        paymentManager.deletePayment(s1);

        assertThat(paymentManager.findPaymentById(s1.getId())).isNull();
        assertThat(paymentManager.findPaymentById(s2.getId())).isNotNull();

    }
    
    @Test (expected = IllegalArgumentException.class)
    public void deleteNullPayment() {
        paymentManager.deletePayment(null);
    }
        
    @Test
    public void delleteNullIdPayment(){
        Payment s2 = sample2PaymentBuilder().id(null).build();
        expectedException.expect(IllegalPaymentException.class);
        paymentManager.deletePayment(s2);

    }
    
    @Test
    public void delleteNonExistingPayment(){
        Payment s2 = sample2PaymentBuilder().id(8L).build();
        expectedException.expect(IllegalEntityException.class);
        paymentManager.deletePayment(s2);

    }
    
    
    private static Comparator<Payment> idComparator = (Payment o1, Payment o2) -> o1.getId().compareTo(o2.getId());
}
