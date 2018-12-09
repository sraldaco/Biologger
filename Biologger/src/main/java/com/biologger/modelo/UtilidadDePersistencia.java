/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author alex aldaco
 */
public class UtilidadDePersistencia {
    private static EntityManagerFactory factory;

    public UtilidadDePersistencia() {
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("biologgerPersistenceUnit");
        }
        return factory;
    }
}
