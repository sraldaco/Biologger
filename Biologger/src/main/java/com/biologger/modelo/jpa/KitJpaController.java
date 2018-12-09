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
import com.biologger.modelo.Usuario;
import com.biologger.modelo.Enlace;
import com.biologger.modelo.Kit;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class KitJpaController implements Serializable {

    public KitJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Kit kit) {
        if (kit.getEnlaces() == null) {
            kit.setEnlaces(new ArrayList<Enlace>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario profesor = kit.getProfesor();
            if (profesor != null) {
                profesor = em.getReference(profesor.getClass(), profesor.getId());
                kit.setProfesor(profesor);
            }
            List<Enlace> attachedEnlaces = new ArrayList<Enlace>();
            for (Enlace enlacesEnlaceToAttach : kit.getEnlaces()) {
                enlacesEnlaceToAttach = em.getReference(enlacesEnlaceToAttach.getClass(), enlacesEnlaceToAttach.getId());
                attachedEnlaces.add(enlacesEnlaceToAttach);
            }
            kit.setEnlaces(attachedEnlaces);
            em.persist(kit);
            if (profesor != null) {
                profesor.getKits().add(kit);
                profesor = em.merge(profesor);
            }
            for (Enlace enlacesEnlace : kit.getEnlaces()) {
                Kit oldKitOfEnlacesEnlace = enlacesEnlace.getKit();
                enlacesEnlace.setKit(kit);
                enlacesEnlace = em.merge(enlacesEnlace);
                if (oldKitOfEnlacesEnlace != null) {
                    oldKitOfEnlacesEnlace.getEnlaces().remove(enlacesEnlace);
                    oldKitOfEnlacesEnlace = em.merge(oldKitOfEnlacesEnlace);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Kit kit) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Kit persistentKit = em.find(Kit.class, kit.getId());
            Usuario profesorOld = persistentKit.getProfesor();
            Usuario profesorNew = kit.getProfesor();
            List<Enlace> enlacesOld = persistentKit.getEnlaces();
            List<Enlace> enlacesNew = kit.getEnlaces();
            List<String> illegalOrphanMessages = null;
            for (Enlace enlacesOldEnlace : enlacesOld) {
                if (!enlacesNew.contains(enlacesOldEnlace)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Enlace " + enlacesOldEnlace + " since its kit field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (profesorNew != null) {
                profesorNew = em.getReference(profesorNew.getClass(), profesorNew.getId());
                kit.setProfesor(profesorNew);
            }
            List<Enlace> attachedEnlacesNew = new ArrayList<Enlace>();
            for (Enlace enlacesNewEnlaceToAttach : enlacesNew) {
                enlacesNewEnlaceToAttach = em.getReference(enlacesNewEnlaceToAttach.getClass(), enlacesNewEnlaceToAttach.getId());
                attachedEnlacesNew.add(enlacesNewEnlaceToAttach);
            }
            enlacesNew = attachedEnlacesNew;
            kit.setEnlaces(enlacesNew);
            kit = em.merge(kit);
            if (profesorOld != null && !profesorOld.equals(profesorNew)) {
                profesorOld.getKits().remove(kit);
                profesorOld = em.merge(profesorOld);
            }
            if (profesorNew != null && !profesorNew.equals(profesorOld)) {
                profesorNew.getKits().add(kit);
                profesorNew = em.merge(profesorNew);
            }
            for (Enlace enlacesNewEnlace : enlacesNew) {
                if (!enlacesOld.contains(enlacesNewEnlace)) {
                    Kit oldKitOfEnlacesNewEnlace = enlacesNewEnlace.getKit();
                    enlacesNewEnlace.setKit(kit);
                    enlacesNewEnlace = em.merge(enlacesNewEnlace);
                    if (oldKitOfEnlacesNewEnlace != null && !oldKitOfEnlacesNewEnlace.equals(kit)) {
                        oldKitOfEnlacesNewEnlace.getEnlaces().remove(enlacesNewEnlace);
                        oldKitOfEnlacesNewEnlace = em.merge(oldKitOfEnlacesNewEnlace);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = kit.getId();
                if (findKit(id) == null) {
                    throw new NonexistentEntityException("The kit with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Kit kit;
            try {
                kit = em.getReference(Kit.class, id);
                kit.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The kit with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Enlace> enlacesOrphanCheck = kit.getEnlaces();
            for (Enlace enlacesOrphanCheckEnlace : enlacesOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Kit (" + kit + ") cannot be destroyed since the Enlace " + enlacesOrphanCheckEnlace + " in its enlaces field has a non-nullable kit field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Usuario profesor = kit.getProfesor();
            if (profesor != null) {
                profesor.getKits().remove(kit);
                profesor = em.merge(profesor);
            }
            em.remove(kit);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Kit> findKitEntities() {
        return findKitEntities(true, -1, -1);
    }

    public List<Kit> findKitEntities(int maxResults, int firstResult) {
        return findKitEntities(false, maxResults, firstResult);
    }

    private List<Kit> findKitEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Kit.class));
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

    public Kit findKit(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Kit.class, id);
        } finally {
            em.close();
        }
    }

    public int getKitCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Kit> rt = cq.from(Kit.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
