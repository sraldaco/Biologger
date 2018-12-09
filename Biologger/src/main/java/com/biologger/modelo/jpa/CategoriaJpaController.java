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
import java.util.ArrayList;
import java.util.List;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class CategoriaJpaController implements Serializable {

    public CategoriaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Categoria categoria) {
        if (categoria.getHijas() == null) {
            categoria.setHijas(new ArrayList<Categoria>());
        }
        if (categoria.getRmcList() == null) {
            categoria.setRmcList(new ArrayList<Rmc>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categoria padre = categoria.getPadre();
            if (padre != null) {
                padre = em.getReference(padre.getClass(), padre.getId());
                categoria.setPadre(padre);
            }
            List<Categoria> attachedHijas = new ArrayList<Categoria>();
            for (Categoria hijasCategoriaToAttach : categoria.getHijas()) {
                hijasCategoriaToAttach = em.getReference(hijasCategoriaToAttach.getClass(), hijasCategoriaToAttach.getId());
                attachedHijas.add(hijasCategoriaToAttach);
            }
            categoria.setHijas(attachedHijas);
            List<Rmc> attachedRmcList = new ArrayList<Rmc>();
            for (Rmc rmcListRmcToAttach : categoria.getRmcList()) {
                rmcListRmcToAttach = em.getReference(rmcListRmcToAttach.getClass(), rmcListRmcToAttach.getId());
                attachedRmcList.add(rmcListRmcToAttach);
            }
            categoria.setRmcList(attachedRmcList);
            em.persist(categoria);
            if (padre != null) {
                padre.getHijas().add(categoria);
                padre = em.merge(padre);
            }
            for (Categoria hijasCategoria : categoria.getHijas()) {
                Categoria oldPadreOfHijasCategoria = hijasCategoria.getPadre();
                hijasCategoria.setPadre(categoria);
                hijasCategoria = em.merge(hijasCategoria);
                if (oldPadreOfHijasCategoria != null) {
                    oldPadreOfHijasCategoria.getHijas().remove(hijasCategoria);
                    oldPadreOfHijasCategoria = em.merge(oldPadreOfHijasCategoria);
                }
            }
            for (Rmc rmcListRmc : categoria.getRmcList()) {
                Categoria oldCategoriaOfRmcListRmc = rmcListRmc.getCategoria();
                rmcListRmc.setCategoria(categoria);
                rmcListRmc = em.merge(rmcListRmc);
                if (oldCategoriaOfRmcListRmc != null) {
                    oldCategoriaOfRmcListRmc.getRmcList().remove(rmcListRmc);
                    oldCategoriaOfRmcListRmc = em.merge(oldCategoriaOfRmcListRmc);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Categoria categoria) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categoria persistentCategoria = em.find(Categoria.class, categoria.getId());
            Categoria padreOld = persistentCategoria.getPadre();
            Categoria padreNew = categoria.getPadre();
            List<Categoria> hijasOld = persistentCategoria.getHijas();
            List<Categoria> hijasNew = categoria.getHijas();
            List<Rmc> rmcListOld = persistentCategoria.getRmcList();
            List<Rmc> rmcListNew = categoria.getRmcList();
            List<String> illegalOrphanMessages = null;
            for (Rmc rmcListOldRmc : rmcListOld) {
                if (!rmcListNew.contains(rmcListOldRmc)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Rmc " + rmcListOldRmc + " since its categoria field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (padreNew != null) {
                padreNew = em.getReference(padreNew.getClass(), padreNew.getId());
                categoria.setPadre(padreNew);
            }
            List<Categoria> attachedHijasNew = new ArrayList<Categoria>();
            for (Categoria hijasNewCategoriaToAttach : hijasNew) {
                hijasNewCategoriaToAttach = em.getReference(hijasNewCategoriaToAttach.getClass(), hijasNewCategoriaToAttach.getId());
                attachedHijasNew.add(hijasNewCategoriaToAttach);
            }
            hijasNew = attachedHijasNew;
            categoria.setHijas(hijasNew);
            List<Rmc> attachedRmcListNew = new ArrayList<Rmc>();
            for (Rmc rmcListNewRmcToAttach : rmcListNew) {
                rmcListNewRmcToAttach = em.getReference(rmcListNewRmcToAttach.getClass(), rmcListNewRmcToAttach.getId());
                attachedRmcListNew.add(rmcListNewRmcToAttach);
            }
            rmcListNew = attachedRmcListNew;
            categoria.setRmcList(rmcListNew);
            categoria = em.merge(categoria);
            if (padreOld != null && !padreOld.equals(padreNew)) {
                padreOld.getHijas().remove(categoria);
                padreOld = em.merge(padreOld);
            }
            if (padreNew != null && !padreNew.equals(padreOld)) {
                padreNew.getHijas().add(categoria);
                padreNew = em.merge(padreNew);
            }
            for (Categoria hijasOldCategoria : hijasOld) {
                if (!hijasNew.contains(hijasOldCategoria)) {
                    hijasOldCategoria.setPadre(null);
                    hijasOldCategoria = em.merge(hijasOldCategoria);
                }
            }
            for (Categoria hijasNewCategoria : hijasNew) {
                if (!hijasOld.contains(hijasNewCategoria)) {
                    Categoria oldPadreOfHijasNewCategoria = hijasNewCategoria.getPadre();
                    hijasNewCategoria.setPadre(categoria);
                    hijasNewCategoria = em.merge(hijasNewCategoria);
                    if (oldPadreOfHijasNewCategoria != null && !oldPadreOfHijasNewCategoria.equals(categoria)) {
                        oldPadreOfHijasNewCategoria.getHijas().remove(hijasNewCategoria);
                        oldPadreOfHijasNewCategoria = em.merge(oldPadreOfHijasNewCategoria);
                    }
                }
            }
            for (Rmc rmcListNewRmc : rmcListNew) {
                if (!rmcListOld.contains(rmcListNewRmc)) {
                    Categoria oldCategoriaOfRmcListNewRmc = rmcListNewRmc.getCategoria();
                    rmcListNewRmc.setCategoria(categoria);
                    rmcListNewRmc = em.merge(rmcListNewRmc);
                    if (oldCategoriaOfRmcListNewRmc != null && !oldCategoriaOfRmcListNewRmc.equals(categoria)) {
                        oldCategoriaOfRmcListNewRmc.getRmcList().remove(rmcListNewRmc);
                        oldCategoriaOfRmcListNewRmc = em.merge(oldCategoriaOfRmcListNewRmc);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = categoria.getId();
                if (findCategoria(id) == null) {
                    throw new NonexistentEntityException("The categoria with id " + id + " no longer exists.");
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
            Categoria categoria;
            try {
                categoria = em.getReference(Categoria.class, id);
                categoria.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The categoria with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Rmc> rmcListOrphanCheck = categoria.getRmcList();
            for (Rmc rmcListOrphanCheckRmc : rmcListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Categoria (" + categoria + ") cannot be destroyed since the Rmc " + rmcListOrphanCheckRmc + " in its rmcList field has a non-nullable categoria field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Categoria padre = categoria.getPadre();
            if (padre != null) {
                padre.getHijas().remove(categoria);
                padre = em.merge(padre);
            }
            List<Categoria> hijas = categoria.getHijas();
            for (Categoria hijasCategoria : hijas) {
                hijasCategoria.setPadre(null);
                hijasCategoria = em.merge(hijasCategoria);
            }
            em.remove(categoria);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Categoria> findCategoriaEntities() {
        return findCategoriaEntities(true, -1, -1);
    }

    public List<Categoria> findCategoriaEntities(int maxResults, int firstResult) {
        return findCategoriaEntities(false, maxResults, firstResult);
    }

    private List<Categoria> findCategoriaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Categoria.class));
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

    public Categoria findCategoria(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Categoria.class, id);
        } finally {
            em.close();
        }
    }

    public int getCategoriaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Categoria> rt = cq.from(Categoria.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
