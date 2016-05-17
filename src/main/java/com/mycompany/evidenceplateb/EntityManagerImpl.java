/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author User
 */
public class EntityManagerImpl implements EntityManager {

    private DataSource dataSource;
    private static final Logger logger = Logger.getLogger(
            EntityManagerImpl.class.getName());

    public EntityManagerImpl(DataSource dataSource){
        this.dataSource = dataSource;
    }
    public EntityManagerImpl(){
        
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createEntity(Entity entity) throws ServiceFailureException {
        checkDataSource();
        validate(entity);
        if (entity.getId() != null) {
            throw new IllegalEntityException("entity id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO ENTITY (e_name) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, entity.getName());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, entity, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            entity.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting entity into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Entity entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        if (entity.getName() == null) {
            throw new IllegalArgumentException("name is null");
        }
    }

    @Override
    public void updateEntity(Entity entity) throws ServiceFailureException {
        checkDataSource();
        validate(entity);
        if (entity.getId() == null) {
            throw new IllegalEntityException("entity id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE ENTITY SET e_name = ? WHERE id = ?");
            st.setString(1, entity.getName());
            st.setLong(2, entity.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, entity, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating entity in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deleteEntity(Entity entity) throws ServiceFailureException {
        checkDataSource();
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        if (entity.getId() == null) {
            throw new IllegalEntityException("entity id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM entity WHERE id = ?");
            st.setLong(1, entity.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, entity, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting entity from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new cz.muni.fi.pv168.common.ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    @Override
    public Entity findEntityById(Long id) throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM ENTITY WHERE id = ?")) {

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return resultSetToEntity(rs);
            }
            return null;
            

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving an entity", ex);
        }
    }

    @Override
    public List<Entity> findAllEntities() throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT * FROM ENTITY")) {

            ResultSet rs = st.executeQuery();

            List<Entity> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToEntity(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all entities", ex);
        }
    }

    private Entity resultSetToEntity(ResultSet rs) throws SQLException {
        Entity entity = new Entity();
        entity.setId(rs.getLong("id"));
        entity.setName(rs.getString("e_name"));
        return entity;
        
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

}
