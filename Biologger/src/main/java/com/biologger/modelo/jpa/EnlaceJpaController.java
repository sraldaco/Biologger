/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo.jpa;

import com.biologger.modelo.Enlace;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.biologger.modelo.Kit;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class EnlaceJpaController implements Serializable {

    public EnlaceJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Enlace enlace) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Kit kit = enlace.getKit();
            if (kit != null) {
                kit = em.getReference(kit.getClass(), kit.getId());
                enlace.setKit(kit);
            }
            em.persist(enlace);
            if (kit != null) {
                kit.getEnlaces().add(enlace);
                kit = em.merge(kit);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Enlace enlace) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Enlace persistentEnlace = em.find(Enlace.class, enlace.getId());
            Kit kitOld = persistentEnlace.getKit();
            Kit kitNew = enlace.getKit();
            if (kitNew != null) {
                kitNew = em.getReference(kitNew.getClass(), kitNew.getId());
                enlace.setKit(kitNew);
            }
            enlace = em.merge(enlace);
            if (kitOld != null && !kitOld.equals(kitNew)) {
                kitOld.getEnlaces().remove(enlace);
                kitOld = em.merge(kitOld);
            }
            if (kitNew != null && !kitNew.equals(kitOld)) {
                kitNew.getEnlaces().add(enlace);
                kitNew = em.merge(kitNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = enlace.getId();
                if (findEnlace(id) == null) {
                    throw new NonexistentEntityException("The enlace with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Enlace enlace;
            try {
                enlace = em.getReference(Enlace.class, id);
                enlace.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The enlace with id " + id + " no longer exists.", enfe);
            }
            Kit kit = enlace.getKit();
            if (kit != null) {
                kit.getEnlaces().remove(enlace);
                kit = em.merge(kit);
            }
            em.remove(enlace);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Enlace> findEnlaceEntities() {
        return findEnlaceEntities(true, -1, -1);
    }

    public List<Enlace> findEnlaceEntities(int maxResults, int firstResult) {
        return findEnlaceEntities(false, maxResults, firstResult);
    }

    private List<Enlace> findEnlaceEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Enlace.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Enlace findEnlace(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Enlace.class, id);
        } finally {
            em.close();
        }
    }

    public int getEnlaceCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Enlace> rt = cq.from(Enlace.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
