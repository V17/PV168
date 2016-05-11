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
public interface EntityManager {
    
    void createEntity(Entity entity) throws ServiceFailureException;
    
    void updateEntity(Entity entity) throws ServiceFailureException;
    
    void deleteEntity(Entity entity) throws ServiceFailureException;
    
    Entity findEntityById(Long id) throws ServiceFailureException;
    
    List<Entity> findAllEntities() throws ServiceFailureException;
}
