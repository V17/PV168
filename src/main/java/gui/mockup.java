/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import com.mycompany.evidenceplateb.Entity;
import com.mycompany.evidenceplateb.EntityManager;
import com.mycompany.evidenceplateb.EntityManagerImpl;
import com.mycompany.evidenceplateb.Payment;
import com.mycompany.evidenceplateb.PaymentManager;
import com.mycompany.evidenceplateb.PaymentManagerImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * @author vozka
 */
public class mockup extends javax.swing.JFrame {

    DataSource datasource;
    private static final Logger LOGGER = Logger.getLogger(mockup.class.getName());
    PaymentManager paymentManager;
    EntityManager entityManager;

    EntityTableModel entityTableModel;
    PaymentTableModel paymentTableModel;

    private SwingWorker swingWorker;
    
    

    private class setUpEntityTable extends SwingWorker<List<Entity>, Void> {
        @Override
        protected List<Entity> doInBackground() throws Exception {
            List<Entity> entities = new ArrayList<>();
            try {
                LOGGER.log(Level.INFO, "Retrieving entities");
                entities = entityManager.findAllEntities();
            }catch(Exception e) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg);
            }
            return entities;
        }

        @Override
        protected void done() {
            try {
                List<Entity> entities = get();
                for(Entity entity: entities) {
                    entityTableModel.addEntity(entity);
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(mockup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     private class setUpPaymentTable extends SwingWorker<List<Payment>, Void> {
        @Override
        protected List<Payment> doInBackground() throws Exception {
            List<Payment> payments = new ArrayList<>();
            try {
                LOGGER.log(Level.INFO, "Retrieving payments");
                payments = paymentManager.findAllPayments();
            }catch(Exception e) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg);
            }
            return payments;
        }

        @Override
        protected void done() {
            try {
                
                List<Payment> payments = get();
                for(Payment payment: payments) {
                    paymentTableModel.addPayment(payment);
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(mockup.class.getName()).log(Level.SEVERE, null, ex);
            }
            updateComboBox();
        }
    }

    private class AddEntitySwingWorker extends SwingWorker<Entity, Void> {

        private final Entity entity;

        public AddEntitySwingWorker(Entity entity) {
            this.entity = entity;
        }

        @Override
        protected Entity doInBackground() throws Exception {
            try {
                LOGGER.log(Level.INFO, "Adding entity");
                entityManager.createEntity(entity);
            } catch (Exception ex) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg);
            }
            return entity;
        }

        @Override
        protected void done() {
            try {
                entityTableModel.addEntity(get());
                updateComboBox();
            } catch (ExecutionException ex) {
                String msg = "User request not displayed";
                LOGGER.log(Level.INFO, msg);
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)", ex);
            }
        }
    }

    private class RemoveEntitySwingWorker extends SwingWorker<Entity, Void> {

        private final Entity entity;

        public RemoveEntitySwingWorker(Entity entity) {
            this.entity = entity;
        }

        @Override
        protected Entity doInBackground() throws Exception {
            try {
   
             LOGGER.log(Level.INFO, "Deleting entity");
                entityManager.deleteEntity(entity);
            } catch (Exception ex) {
                String msg = "Deleting failed";
                LOGGER.log(Level.INFO, msg);
            }
            return entity;
        }

        @Override
        protected void done() {
            try {
                entityTableModel.removeEntity(get());
                updateComboBox();
            } catch (ExecutionException ex) {
                String msg = "User request not displayed";
                LOGGER.log(Level.INFO, msg);
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)", ex);
            }
        }
    }
    
    private class UpdateEntitySwingWorker extends SwingWorker<Void, Void> {

        private final Entity entity;

        public UpdateEntitySwingWorker(Entity entity) {
            this.entity = entity;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                LOGGER.log(Level.INFO, "Updating entity");
                entityManager.updateEntity(entity);
            } catch (Exception ex) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg);
            }
            return null;
        }
    }

    private class AddPaymentSwingWorker extends SwingWorker<Payment, Void> {

        Payment payment;
        
        public AddPaymentSwingWorker(Payment payment) {
            this.payment = payment;
        }
    
        @Override
        protected Payment doInBackground() throws Exception {
                       
            try {
                LOGGER.log(Level.INFO, "Adding payment");
                paymentManager.createPayment(payment);
            } catch (Exception ex) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg, ex);
            }
            return payment;
        }
        
         @Override
        protected void done() {
            try {
                paymentTableModel.addPayment(get());
                updateComboBox();
            } catch (ExecutionException ex) {
                String msg = "User request not displayed";
                LOGGER.log(Level.INFO, msg);
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)", ex);
            }
        }
    }
    
    private class removePaymentSwingWorker extends SwingWorker<Void, Void> {

        private final Payment payment;

        public removePaymentSwingWorker(Payment payment) {
            this.payment = payment;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
   
             LOGGER.log(Level.INFO, "Deleting entity");
                paymentManager.deletePayment(payment);
            } catch (Exception ex) {
                String msg = "Deleting failed";
                LOGGER.log(Level.INFO, msg);
            }
            return null;
        }

        @Override
        protected void done() {
                paymentTableModel.removePayment(payment);
                updateComboBox();
        }
    }
    
    private class updatePaymentSwingWorker extends SwingWorker<Void, Void> {

        private final Payment payment;

        public updatePaymentSwingWorker(Payment payment) {
            this.payment = payment;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                LOGGER.log(Level.INFO, "Updating payment");
                paymentManager.updatePayment(payment);
            } catch (Exception ex) {
                String msg = "User request failed";
                LOGGER.log(Level.INFO, msg);
            }
            return null;
        }
    }

    public mockup() {
        try {
            setUp();
        } catch (Exception ex) {
            String msg = "Database setup failed.";
            LOGGER.log(Level.SEVERE, msg, ex);
        }
        

        initComponents();
        
        paymentManager = new PaymentManagerImpl(datasource);
        entityManager = new EntityManagerImpl(datasource);
        entityTableModel = (EntityTableModel) EntityTable.getModel();
        paymentTableModel = (PaymentTableModel) PaymentTable.getModel();
        entityTableModel.addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                String str = e.toString();
                LOGGER.log(Level.INFO, str);
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                String name = (String) model.getValueAt(row, column);
                Long id = (Long) model.getValueAt(row, 0);
                Entity entity = new Entity(id, name);
                updateComboBox();
                swingWorker = new UpdateEntitySwingWorker(entity);
                swingWorker.execute();
                swingWorker = null;
            }
        });
        
        paymentTableModel.addTableModelListener((TableModelEvent e) ->{
            if (e.getType() == TableModelEvent.UPDATE) {
                String str = e.toString();
                LOGGER.log(Level.INFO, str);
                int row = e.getFirstRow();
                Payment payment = new Payment();
                payment.setId((Long) paymentTableModel.getValueAt(row, 0));
                payment.setSum((BigDecimal) paymentTableModel.getValueAt(row, 1));
                payment.setDate((LocalDate) paymentTableModel.getValueAt(row, 2));
                payment.setFrom((Entity) paymentTableModel.getValueAt(row, 3));
                payment.setFrom((Entity) paymentTableModel.getValueAt(row, 4));
                swingWorker = new updatePaymentSwingWorker(payment);
                swingWorker.execute();
                swingWorker = null;
            }
        });

        swingWorker = new setUpEntityTable();
        swingWorker.execute();
        swingWorker = null;
        
        swingWorker = new setUpPaymentTable();
        swingWorker.execute();
        swingWorker = null;
        
    }

    private void setUp() {
        Properties configFile = new Properties();
        try {
            configFile.load(new FileInputStream("src/main/resources/com/mycompany/evidenceplateb/config.properties"));
            ClientDataSource ds = new ClientDataSource();
            //ds.setServerName("localhost");
            ds.setDatabaseName(configFile.getProperty("name"));
            ds.setPassword(configFile.getProperty("password"));
            ds.setUser(configFile.getProperty("username"));
            datasource = ds;
        } catch (IOException ex) {
            Logger.getLogger(mockup.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName(configFile.getProperty("name"));
        dataSource.setPassword(configFile.getProperty("password"));
        dataSource.setUser(configFile.getProperty("username"));
        //dataSource.setCreateDatabase("create");
        embeddedDataSource = dataSource;*/
    }

    private void updateComboBox() {
        fromComboBox.removeAllItems();
        ToComboBox.removeAllItems();
        combo.removeAllItems();
        for (Entity e : entityTableModel.getAllEntities()) {
            fromComboBox.addItem(e.toString());
            ToComboBox.addItem(e.toString());
            combo.addItem(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        SumTextFiield = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        AddPaymentButton = new javax.swing.JButton();
        removePaymentButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jDateChooser = new com.toedter.calendar.JDateChooser();
        jScrollPane3 = new javax.swing.JScrollPane();
        PaymentTable = new javax.swing.JTable();
        ToComboBox = new javax.swing.JComboBox<>();
        fromComboBox = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        EntityNameInput = new javax.swing.JTextField();
        AddEntityButton = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        RemoveEntityButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        EntityTable = new javax.swing.JTable();

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/mycompany/evidenceplateb/Bundle"); // NOI18N
        setTitle(bundle.getString("PAYMENT MANAGER")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("payments_border"))); // NOI18N

        SumTextFiield.setText(bundle.getString("filter_sum_field")); // NOI18N
        SumTextFiield.setMinimumSize(new java.awt.Dimension(6, 25));
        SumTextFiield.setPreferredSize(new java.awt.Dimension(70, 25));
        SumTextFiield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SumTextFiieldActionPerformed(evt);
            }
        });

        jButton1.setText(bundle.getString("payment_filter_button")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(53, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(53, 25));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        AddPaymentButton.setText(bundle.getString("add_payment_button")); // NOI18N
        AddPaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddPaymentButtonActionPerformed(evt);
            }
        });

        removePaymentButton.setText(bundle.getString("remove_payment_button")); // NOI18N
        removePaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePaymentButtonActionPerformed(evt);
            }
        });

        jButton4.setText(bundle.getString("edit_payment_button")); // NOI18N

        jDateChooser.setPreferredSize(new java.awt.Dimension(120, 25));
        jDateChooser.setRequestFocusEnabled(false);

        PaymentTable.setModel(new PaymentTableModel());
        jScrollPane3.setViewportView(PaymentTable);
        PaymentTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(combo));
        PaymentTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(combo));

        fromComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(SumTextFiield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(30, 30, 30)
                                .addComponent(fromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(ToComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(AddPaymentButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4)
                                .addGap(39, 39, 39)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(removePaymentButton)
                                .addContainerGap())))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(SumTextFiield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ToComboBox)
                            .addComponent(fromComboBox))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddPaymentButton)
                    .addComponent(jButton4)
                    .addComponent(removePaymentButton))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("entities_border"))); // NOI18N

        EntityNameInput.setText(bundle.getString("new_entity_field")); // NOI18N

        AddEntityButton.setText(bundle.getString("add_entity_button")); // NOI18N
        AddEntityButton.setPreferredSize(new java.awt.Dimension(82, 23));
        AddEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddEntityButtonActionPerformed(evt);
            }
        });

        jButton6.setText(bundle.getString("edit_entity_button")); // NOI18N
        jButton6.setPreferredSize(new java.awt.Dimension(82, 23));

        RemoveEntityButton.setText(bundle.getString("remove_entity_button")); // NOI18N
        RemoveEntityButton.setPreferredSize(new java.awt.Dimension(82, 23));
        RemoveEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveEntityButtonActionPerformed(evt);
            }
        });

        EntityTable.setModel(new EntityTableModel());
        jScrollPane4.setViewportView(EntityTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(EntityNameInput)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(RemoveEntityButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddEntityButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RemoveEntityButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(EntityNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddEntityButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SumTextFiieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SumTextFiieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SumTextFiieldActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void AddEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddEntityButtonActionPerformed
        Entity entity = new Entity();
        entity.setName(EntityNameInput.getText());
        swingWorker = new AddEntitySwingWorker(entity);
        swingWorker.execute();
        swingWorker = null;
    }//GEN-LAST:event_AddEntityButtonActionPerformed

    private void RemoveEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveEntityButtonActionPerformed
        Entity entity = new Entity();
        try {
            entity.setId((Long) entityTableModel.getValueAt(EntityTable.getSelectedRow(), 0));
            entity.setName((String) entityTableModel.getValueAt(EntityTable.getSelectedRow(), 1));
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "No row selected";
            LOGGER.log(Level.INFO, msg);
        }
        swingWorker = new RemoveEntitySwingWorker(entity);
        swingWorker.execute();
        swingWorker = null;
    }//GEN-LAST:event_RemoveEntityButtonActionPerformed

    private void fromComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromComboBoxActionPerformed

    }//GEN-LAST:event_fromComboBoxActionPerformed

    private void AddPaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddPaymentButtonActionPerformed
        Payment payment = new Payment();
        payment.setSum(new BigDecimal(SumTextFiield.getText()));
        payment.setDate(jDateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        
        String name1 = (String) fromComboBox.getSelectedItem();
        Entity entity = new Entity(entityTableModel.findEntityId(name1), name1);
        payment.setFrom(entity);
        
        String name2 = (String) ToComboBox.getSelectedItem();
        Entity entity2 =  new Entity(entityTableModel.findEntityId(name2), name2);      
        payment.setTo(entity2);
        combo.addItem(entity2);
        
        swingWorker = new AddPaymentSwingWorker(payment);
        swingWorker.execute();
        swingWorker = null;
    }//GEN-LAST:event_AddPaymentButtonActionPerformed

    private void removePaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePaymentButtonActionPerformed
        Payment payment = new Payment();
        try {
            payment.setId((Long) paymentTableModel.getValueAt(PaymentTable.getSelectedRow(), 0));
            payment.setSum((BigDecimal) paymentTableModel.getValueAt(PaymentTable.getSelectedRow(), 1));
            payment.setDate((LocalDate) paymentTableModel.getValueAt(PaymentTable.getSelectedRow(), 2));
            payment.setFrom((Entity) paymentTableModel.getValueAt(PaymentTable.getSelectedRow(), 3));
            payment.setFrom((Entity) paymentTableModel.getValueAt(PaymentTable.getSelectedRow(), 4));
            
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "No row selected";
            LOGGER.log(Level.INFO, msg);
        }
        swingWorker = new removePaymentSwingWorker(payment);
        swingWorker.execute();
        swingWorker = null;
    }//GEN-LAST:event_removePaymentButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(mockup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(mockup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(mockup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(mockup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new mockup().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddEntityButton;
    private javax.swing.JButton AddPaymentButton;
    private javax.swing.JTextField EntityNameInput;
    private javax.swing.JTable EntityTable;
    private javax.swing.JTable PaymentTable;
    private JComboBox<Entity> combo = new JComboBox<>();
    private javax.swing.JButton RemoveEntityButton;
    private javax.swing.JTextField SumTextFiield;
    private javax.swing.JComboBox<String> ToComboBox;
    private javax.swing.JComboBox<String> fromComboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private com.toedter.calendar.JDateChooser jDateChooser;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton removePaymentButton;
    // End of variables declaration//GEN-END:variables
}
