/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalPaymentException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author User
 */
public class PaymentManagerImpl implements PaymentManager  {
 
    private DataSource dataSource;
    private static final Logger logger = Logger.getLogger(
            PaymentManagerImpl.class.getName());

              
     
    @Override
    public void createPayment(Payment payment) {
        checkDataSource();
        validate(payment);
        if (payment.getId() != null) {
            throw new IllegalArgumentException("payment id is already set");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Payment (p_sum,p_date,p_from,p_to) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setBigDecimal(1, payment.getSum());
            st.setDate(2, toSqlDate(payment.getDate()));
            st.setLong(3, payment.getFrom().getId());
            st.setLong(4, payment.getTo().getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, payment, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            payment.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting payment into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
        
    }

    @Override
    public void updatePayment(Payment payment) {
        checkDataSource();
        validate(payment);
        if (payment.getId() == null) {
            throw new IllegalPaymentException("entity id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Payment SET  p_sum = ?, p_date = ?, p_from = ?, p_to = ? WHERE p_id = ?");
            st.setBigDecimal(1, payment.getSum());
            st.setDate(2, toSqlDate(payment.getDate()));
            st.setLong(3, payment.getFrom().getId());
            st.setLong(4, payment.getTo().getId());
            st.setLong(5, payment.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, payment, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating payment in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deletePayment(Payment payment) {
         checkDataSource();
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }        
        if (payment.getId() == null) {
            throw new IllegalPaymentException("payment id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Payment WHERE p_id = ?");
            st.setLong(1, payment.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, payment, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting payment from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);  
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Payment findPaymentById(Long id) {
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Payment WHERE P_id = ?");
            st.setLong(1, id);
            return executeQueryForPayment(st);
        } catch (SQLException ex) {
            String msg = "Error when getting payment with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Payment findPaymentByTo(Entity entity) {
        checkDataSource();
        
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Payment WHERE p_to = ?");
            st.setLong(1, entity.getId());
            return executeQueryForPayment(st);
        } catch (SQLException ex) {
            String msg = "Error when getting payment with to = " + entity + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Payment findPaymentByFrom(Entity entity) {
        checkDataSource();
        
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Payment WHERE p_from = ?");
            st.setLong(1, entity.getId());
            return executeQueryForPayment(st);
        } catch (SQLException ex) {
            String msg = "Error when getting payment with from = " + entity + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Payment> findAllPayments() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Payment");
            return executeQueryForMultiplePayments(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all payments from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }          
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    private static void validate(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }
        if (payment.getSum() == null || payment.getSum().intValue() < 0) {
            throw new IllegalStateException("sum is negative number");
        }
        if (payment.getDate() ==  null) {
            throw new IllegalArgumentException("date is null");
        }
        if (payment.getFrom() == null) {
            throw new IllegalArgumentException("from is null"); 
        }
        if (payment.getTo() == null) {
            throw new IllegalArgumentException("to is null");
        }
    }

    
    
    ///Static ??????
    Payment executeQueryForPayment(PreparedStatement st) throws SQLException, cz.muni.fi.pv168.common.ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Payment result = rowToPayment(rs);                
            if (rs.next()) {
                throw new cz.muni.fi.pv168.common.ServiceFailureException(
                        "Internal integrity error: more payments with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    List<Payment> executeQueryForMultiplePayments(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Payment> result = new ArrayList<Payment>();
        while (rs.next()) {
            result.add(rowToPayment(rs));
        }
        return result;
    }
    
    private Payment rowToPayment(ResultSet rs) throws SQLException {
        Payment result = new Payment();
        result.setId(rs.getLong("p_id"));
        result.setSum(rs.getBigDecimal("P_SUM"));

        EntityManagerImpl manager = new EntityManagerImpl();
        manager.setDataSource(dataSource);
        result.setDate(toLocalDate(rs.getDate("P_Date")));
        result.setTo(manager.findEntityById(rs.getLong("P_TO")));
        result.setFrom(manager.findEntityById(rs.getLong("P_FROM")));
        return result;
    }
    
    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
    
    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
}
