/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import java.sql.Connection;
import java.sql.SQLException;
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
 * @author vozka
 */
public class EntityManagerImplTest {
    
    
    private DataSource dataSource;
    private EntityManagerImpl manager;
    
    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        manager = new EntityManagerImpl();
        DBUtils.executeSqlScript(dataSource,EntityManager.class.getResource("createTables.sql"));
        manager.setDataSource(dataSource);
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
        ds.setDatabaseName("memory:entitymgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    private EntityBuilder sample1EntityBuilder() {
        return new EntityBuilder()
                .name("1");
    }
    
    private EntityBuilder sample2EntityBuilder() {
        return new EntityBuilder()
                .name("2");
    }
    
    
    @Test
    public void createEntity() {
        Entity entity = sample1EntityBuilder().build();
        manager.createEntity(entity);

        Long entityId = entity.getId();
        assertThat(entityId).isNotNull();

        assertThat(manager.findEntityById(entityId))
                .isNotSameAs(entity)
                .isEqualToComparingFieldByField(entity);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        manager.createEntity(null);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createEntityWithExistingId() {
        Entity entity = sample1EntityBuilder().id(1L).build();
        manager.createEntity(entity);
       
    }
    
    @Test
    public void createEntityWithNullName() {
        Entity entity = sample1EntityBuilder().id(2L).name(null).build();       
        assertThatThrownBy(() -> manager.createEntity(entity))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
        
    private void testUpdateEntity(Operation<Entity> updateOperation) {
        Entity entityForUpdate = sample1EntityBuilder().build();
        Entity anotherEntity = sample2EntityBuilder().build();
        manager.createEntity(entityForUpdate);
        manager.createEntity(anotherEntity);

        updateOperation.callOn(entityForUpdate);

        manager.updateEntity(entityForUpdate);
        assertThat(manager.findEntityById(entityForUpdate.getId()))
                .isEqualToComparingFieldByField(entityForUpdate);
        
        assertThat(manager.findEntityById(anotherEntity.getId()))
                .isEqualToComparingFieldByField(anotherEntity);
    }
    
    @Test
    public void updateEntityName() {
        testUpdateEntity((entity) -> entity.setName("New"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateEntityNameNull() {
        testUpdateEntity((entity) -> entity.setName(null));
    }

    // Test also if attemtpt to call update with invalid body throws
    // the correct exception.

    @Test(expected = IllegalArgumentException.class)
    public void updateNullEntity() {
        manager.updateEntity(null);
    }

    @Test
    public void updateBodyWithNullId() {
        Entity entity = sample1EntityBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateEntity(entity);
    }

    @Test
    public void updateNonExistingEntity() {
        Entity entity = sample1EntityBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.updateEntity(entity);
    }

    /**
    @Test
    public void updateBodyWithNullName() {
        Body body = sampleJoeBodyBuilder().build();
        manager.createBody(body);
        body.setName(null);

        expectedException.expect(ValidationException.class);
        manager.updateBody(body);
    }*/
    
    @Test
    public void findAllEntities() {

        assertThat(manager.findAllEntities().isEmpty());

        Entity s1 = sample1EntityBuilder().build();
        Entity s2 = sample2EntityBuilder().build();

        manager.createEntity(s1);
        manager.createEntity(s2);

        assertThat(manager.findAllEntities())
                .usingFieldByFieldElementComparator()
                .containsOnly(s1,s2);
    }
    
    @Test
    public void deleteEntity() {
        Entity s1 = sample1EntityBuilder().build();
        Entity s2 = sample2EntityBuilder().build();
        manager.createEntity(s1);
        manager.createEntity(s2);

        assertThat(manager.findEntityById(s1.getId())).isNotNull();
        assertThat(manager.findEntityById(s2.getId())).isNotNull();

        manager.deleteEntity(s1);

        assertThat(manager.findEntityById(s1.getId())).isNull();
        assertThat(manager.findEntityById(s2.getId())).isNotNull();

    }
    
    @Test (expected = IllegalArgumentException.class)
    public void deleteNullEntity() {
        manager.deleteEntity(null);
    }
        
    @Test
    public void delleteNullIdEntity(){
        Entity s2 = sample2EntityBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteEntity(s2);

    }
    
    @Test
    public void delleteNonExistingEntity(){
        Entity s2 = sample2EntityBuilder().id(8L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteEntity(s2);

    }
    
    
    private static final Comparator<Entity> idComparator = (Entity o1, Entity o2) -> o1.getId().compareTo(o2.getId());

}
