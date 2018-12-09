/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.pedido.modelo;

import com.biologger.modelo.Pedido;
import com.biologger.modelo.jpa.PedidoJpaController;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * @author alex aldaco
 */
public class PedidoJpa extends PedidoJpaController {

    public PedidoJpa(EntityManagerFactory emf) {
        super(emf);
    }
    
    public int countfiltrarPedidos(String estado, String correo) {
        EntityManager em = getEntityManager();
        String query = "SELECT COUNT(p.id) FROM Pedido p";
        if (estado != null && correo != null) {
            query += " WHERE p.estado = :estado AND p.usuario.correo = :correo";
        } else {
            if (estado != null) {
                query += " WHERE p.estado = :estado";
            }
            if (correo != null) {
                query += " WHERE p.usuario.correo = :correo";
            }
        }
        try {
            Query q = em.createQuery(query);
            if (estado != null && correo != null) {
                q.setParameter("estado", estado);
                q.setParameter("correo", correo);
            } else {
                if (estado != null) {
                    q.setParameter("estado", estado);
                }
                if (correo != null) {
                    q.setParameter("correo", correo);
                }
            }
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
        
    }
    
    public List<Pedido> filtrarPedidos(String estado, String correo,int maxResults, int firstResult, String orden, String modo) {
        EntityManager em = getEntityManager();
        String query = "SELECT p FROM Pedido p";
        if (estado != null && correo != null) {
            query += " WHERE p.estado = :estado AND p.usuario.correo = :correo";
        } else {
            if (estado != null) {
                query += " WHERE p.estado = :estado";
            }
            if (correo != null) {
                query += " WHERE p.usuario.correo = :correo";
            }
        }
        query += " ORDER BY";
        query += orden.equals("id") ? " p.id " : " p.usuario.correo ";
        query += modo;
        try {
            Query q = em.createQuery(query);
            q.setFirstResult(firstResult);
            q.setMaxResults(maxResults);
            if (estado != null && correo != null) {
                q.setParameter("estado", estado);
                q.setParameter("correo", correo);
            } else {
                if (estado != null) {
                    q.setParameter("estado", estado);
                }
                if (correo != null) {
                    q.setParameter("correo", correo);
                }
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Pedido> pedidosPendientes() {
        EntityManager em = getEntityManager();
        try {
             return em.createQuery("SELECT p FROM Pedido p WHERE p.estado = 'Pendiente'")
                     .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Pedido> pedidosActivos() {
        EntityManager em = getEntityManager();
        try {
             return em.createQuery("SELECT p FROM Pedido p WHERE p.estado = 'Activo'")
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
