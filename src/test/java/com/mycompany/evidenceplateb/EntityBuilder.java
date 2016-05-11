/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.evidenceplateb;

import com.mycompany.evidenceplateb.Entity;

/**
 *
 * @author User
 */
public class EntityBuilder {
    private Long id;
    private String name;
    
    public EntityBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public EntityBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public Entity build() {
        Entity entity = new Entity();
        entity.setId(id);
        entity.setName(name);
        return entity; 
    }
}
