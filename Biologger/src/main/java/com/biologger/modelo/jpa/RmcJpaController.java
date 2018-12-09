/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo.jpa;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.biologger.modelo.Categoria;
import com.biologger.modelo.Material;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class RmcJpaController implements Serializable {

    public RmcJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Rmc rmc) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categoria categoria = rmc.getCategoria();
            if (categoria != null) {
                categoria = em.getReference(categoria.getClass(), categoria.getId());
                rmc.setCategoria(categoria);
            }
            Material material = rmc.getMaterial();
            if (material != null) {
                material = em.getReference(material.getClass(), material.getId());
                rmc.setMaterial(material);
            }
            em.persist(rmc);
            if (categoria != null) {
                categoria.getRmcList().add(rmc);
                categoria = em.merge(categoria);
            }
            if (material != null) {
                material.getRmcList().add(rmc);
                material = em.merge(material);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Rmc rmc) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Rmc persistentRmc = em.find(Rmc.class, rmc.getId());
            Categoria categoriaOld = persistentRmc.getCategoria();
            Categoria categoriaNew = rmc.getCategoria();
            Material materialOld = persistentRmc.getMaterial();
            Material materialNew = rmc.getMaterial();
            if (categoriaNew != null) {
                categoriaNew = em.getReference(categoriaNew.getClass(), categoriaNew.getId());
                rmc.setCategoria(categoriaNew);
            }
            if (materialNew != null) {
                materialNew = em.getReference(materialNew.getClass(), materialNew.getId());
                rmc.setMaterial(materialNew);
            }
            rmc = em.merge(rmc);
            if (categoriaOld != null && !categoriaOld.equals(categoriaNew)) {
                categoriaOld.getRmcList().remove(rmc);
                categoriaOld = em.merge(categoriaOld);
            }
            if (categoriaNew != null && !categoriaNew.equals(categoriaOld)) {
                categoriaNew.getRmcList().add(rmc);
                categoriaNew = em.merge(categoriaNew);
            }
            if (materialOld != null && !materialOld.equals(materialNew)) {
                materialOld.getRmcList().remove(rmc);
                materialOld = em.merge(materialOld);
            }
            if (materialNew != null && !materialNew.equals(materialOld)) {
                materialNew.getRmcList().add(rmc);
                materialNew = em.merge(materialNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = rmc.getId();
                if (findRmc(id) == null) {
                    throw new NonexistentEntityException("The rmc with id " + id + " no longer exists.");
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
            Rmc rmc;
            try {
                rmc = em.getReference(Rmc.class, id);
                rmc.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The rmc with id " + id + " no longer exists.", enfe);
            }
            Categoria categoria = rmc.getCategoria();
            if (categoria != null) {
                categoria.getRmcList().remove(rmc);
                categoria = em.merge(categoria);
            }
            Material material = rmc.getMaterial();
            if (material != null) {
                material.getRmcList().remove(rmc);
                material = em.merge(material);
            }
            em.remove(rmc);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Rmc> findRmcEntities() {
        return findRmcEntities(true, -1, -1);
    }

    public List<Rmc> findRmcEntities(int maxResults, int firstResult) {
        return findRmcEntities(false, maxResults, firstResult);
    }

    private List<Rmc> findRmcEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Rmc.class));
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

    public Rmc findRmc(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Rmc.class, id);
        } finally {
            em.close();
        }
    }

    public int getRmcCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Rmc> rt = cq.from(Rmc.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
