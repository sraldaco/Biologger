/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.usuario.modelo;

import com.biologger.modelo.Usuario;
import com.biologger.modelo.jpa.UsuarioJpaController;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * @author alex aldaco
 */
public class UsuarioJpa extends UsuarioJpaController {
    
    public UsuarioJpa(EntityManagerFactory emf) {
        super(emf);
    }
    
    public Usuario buscarUsuarioNombreUsuario(String nombreUsuario) {
        EntityManager em = getEntityManager();
        try {
            List<Usuario> usuarios;
            String query = "SELECT u FROM Usuario u WHERE LOWER(u.nombreUsuario) = LOWER(:nombreUsuario)";
            usuarios = em.createQuery(query)
                         .setParameter("nombreUsuario", nombreUsuario)
                         .getResultList();
            if (!usuarios.isEmpty()) {
                return usuarios.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public Usuario buscarUsuarioLogin(String nombreUsuario) {
        EntityManager em = getEntityManager();
        try {
            List<Usuario> usuarios;
            String query = "SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario";
            usuarios = em.createQuery(query)
                         .setParameter("nombreUsuario", nombreUsuario)
                         .getResultList();
            if (!usuarios.isEmpty()) {
                return usuarios.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public Usuario buscarUsuarioCorreo(String correo) {
        EntityManager em = getEntityManager();
        try {
            List<Usuario> usuarios = null;
            String query = "SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)";
            usuarios = em.createQuery(query)
                         .setParameter("correo", correo)
                         .getResultList();
            if (!usuarios.isEmpty()) {
                return usuarios.get(0);
            }
            return null;
        } finally {
            em.close();
        }
    }
    
    public List<Usuario> findUsuarioEntitiesFilter(
            Map<String,String> params,int maxResults, int firstResult, String orden, String modo) {
        EntityManager em = getEntityManager();
        String query ="SELECT u FROM Usuario u";
        int i = 1;
        if (!params.isEmpty()) {
            query += " WHERE ";
            for (String key : params.keySet()) {
                if (i > 1) {
                    query += " AND ";
                }
                switch (key) {
                    case "nombre" :
                        query += "LOWER(u." + key + ") LIKE LOWER(concat('%', :nombre,'%'))";
                        break;
                    default :
                        query += "u." + key + " = :" + key;
                }
                i++;
            }
        }
        query += " ORDER BY u." + orden + " " + modo;
        try {
            Query q = em.createQuery(query)
                .setMaxResults(maxResults)
                .setFirstResult(firstResult);
            if (!params.isEmpty()) {
                for (String key : params.keySet()) {
                    switch (key) {
                        case "rol" :
                            q.setParameter(key,Integer.parseInt(params.get(key)));
                            break;
                        case "activo" :
                            boolean value = params.get(key).equals("true") ? true : false;
                            q.setParameter(key, value);
                            break;
                        case "nombre" :
                            q.setParameter(key,params.get(key));
                            break;
                        default:
                            q.setParameter(key,params.get(key));
                    }
                }
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }
    
    public int countUsuarioEntitiesFilter(Map<String,String> params) {
        EntityManager em = getEntityManager();
        String query ="SELECT COUNT(u.id) FROM Usuario u ";
        int i = 1;
        if (!params.isEmpty()) {
            query += " WHERE ";
            for (String key : params.keySet()) {
                if (i > 1) {
                    query += " AND ";
                }
                switch (key) {
                    case "nombre" :
                        query += "LOWER(u." + key + ") LIKE LOWER(concat('%', :nombre,'%'))";
                        break;
                    default :
                        query += "u." + key + " = :" + key;
                }
                i++;
            }
        }
        try {
            Query q = em.createQuery(query);
            if (!params.isEmpty()) {
                for (String key : params.keySet()) {
                    switch (key) {
                        case "rol" :
                            q.setParameter(key,Integer.parseInt(params.get(key)));
                            break;
                        case "activo" :
                            boolean value = params.get(key).equals("true") ? true : false;
                            q.setParameter(key, value);
                            break;
                        case "nombre" :
                            q.setParameter(key,params.get(key));
                            break;
                        default:
                            q.setParameter(key,params.get(key));
                    }
                }
            }
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
