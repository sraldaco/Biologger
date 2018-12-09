/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.usuario.modelo;

import com.biologger.modelo.Profesor;
import com.biologger.modelo.jpa.ProfesorJpaController;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alexa
 */
public class ProfesorJpa extends ProfesorJpaController {
    
    public ProfesorJpa(EntityManagerFactory emf) {
        super(emf);
    }
    
    public Profesor buscarProfesorNumero(String numeroTrabajdor) {
        EntityManager em = getEntityManager();
        try {
            List<Profesor> profesores = null;
            profesores = em.createNamedQuery("Profesor.findByNumero")
                         .setParameter("numero", numeroTrabajdor)
                         .getResultList();
            if (!profesores.isEmpty()) {
                return profesores.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public List<Profesor> obtenerPeticionesPendientes() {
        EntityManager em = getEntityManager();
        try {
            List<Profesor> profesores = 
                    em.createQuery("SELECT p FROM Profesor p WHERE p.validado = :validado ORDER BY p.id DESC")
                    .setParameter("validado",false)
                    .getResultList();
            return profesores;
        } finally {
            em.close();
        }
    }
    
    public List<Profesor> obtenerPeticionesProcesadas(String lista) {
        EntityManager em = getEntityManager();
        String query;
        switch (lista) {
            case "aceptadas" :
                query = "SELECT p FROM Profesor p WHERE p.validado = :validado AND p.usuario.rol = 2 ORDER BY p.id DESC";
                break;
            case "rechazadas" :
                query = "SELECT p FROM Profesor p WHERE p.validado = :validado AND p.usuario.rol = 3 ORDER BY p.id DESC";
                break;
            default: 
                query = "SELECT p FROM Profesor p WHERE p.validado = :validado ORDER BY p.id DESC";
        }
        try {
            List<Profesor> profesores = 
                    em.createQuery(query)
                    .setParameter("validado",true)
                    .getResultList();
            return profesores;
        } finally {
            em.close();
        }
    }
}

