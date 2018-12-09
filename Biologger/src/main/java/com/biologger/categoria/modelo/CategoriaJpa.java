/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.categoria.modelo;

import com.biologger.modelo.Categoria;
import com.biologger.modelo.jpa.CategoriaJpaController;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * @author alex aldaco
 */
public class CategoriaJpa extends CategoriaJpaController {

    public CategoriaJpa(EntityManagerFactory emf) {
        super(emf);
    }
    
    public Categoria buscarCategoriaNombre(String nombre) {
        EntityManager em = getEntityManager();
        List<Categoria> categorias = em.createNamedQuery("Categoria.findByNombre")
                .setParameter("nombre", nombre).getResultList();
        if (!categorias.isEmpty()) {
            return categorias.get(0);
        }
        return null;
    }
    
    public List<Categoria> getCategoriasRoot() {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT c FROM Categoria c WHERE c.padre = null");
        return query.getResultList();
    }
    
    public List<Categoria> getCategoriasEdit(int id) {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("SELECT c FROM Categoria c WHERE c.id != :id")
                .setParameter("id", id);
        return query.getResultList();
    }
    
}
