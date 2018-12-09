/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo.jpa;

import com.biologger.modelo.Material;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.biologger.modelo.Pedido;
import com.biologger.modelo.Rmc;
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
public class MaterialJpaController implements Serializable {

    public MaterialJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Material material) {
        if (material.getRmcList() == null) {
            material.setRmcList(new ArrayList<Rmc>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedido = material.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getId());
                material.setPedido(pedido);
            }
            List<Rmc> attachedRmcList = new ArrayList<Rmc>();
            for (Rmc rmcListRmcToAttach : material.getRmcList()) {
                rmcListRmcToAttach = em.getReference(rmcListRmcToAttach.getClass(), rmcListRmcToAttach.getId());
                attachedRmcList.add(rmcListRmcToAttach);
            }
            material.setRmcList(attachedRmcList);
            em.persist(material);
            if (pedido != null) {
                pedido.getMateriales().add(material);
                pedido = em.merge(pedido);
            }
            for (Rmc rmcListRmc : material.getRmcList()) {
                Material oldMaterialOfRmcListRmc = rmcListRmc.getMaterial();
                rmcListRmc.setMaterial(material);
                rmcListRmc = em.merge(rmcListRmc);
                if (oldMaterialOfRmcListRmc != null) {
                    oldMaterialOfRmcListRmc.getRmcList().remove(rmcListRmc);
                    oldMaterialOfRmcListRmc = em.merge(oldMaterialOfRmcListRmc);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Material material) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Material persistentMaterial = em.find(Material.class, material.getId());
            Pedido pedidoOld = persistentMaterial.getPedido();
            Pedido pedidoNew = material.getPedido();
            List<Rmc> rmcListOld = persistentMaterial.getRmcList();
            List<Rmc> rmcListNew = material.getRmcList();
            List<String> illegalOrphanMessages = null;
            for (Rmc rmcListOldRmc : rmcListOld) {
                if (!rmcListNew.contains(rmcListOldRmc)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Rmc " + rmcListOldRmc + " since its material field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getId());
                material.setPedido(pedidoNew);
            }
            List<Rmc> attachedRmcListNew = new ArrayList<Rmc>();
            for (Rmc rmcListNewRmcToAttach : rmcListNew) {
                rmcListNewRmcToAttach = em.getReference(rmcListNewRmcToAttach.getClass(), rmcListNewRmcToAttach.getId());
                attachedRmcListNew.add(rmcListNewRmcToAttach);
            }
            rmcListNew = attachedRmcListNew;
            material.setRmcList(rmcListNew);
            material = em.merge(material);
            if (pedidoOld != null && !pedidoOld.equals(pedidoNew)) {
                pedidoOld.getMateriales().remove(material);
                pedidoOld = em.merge(pedidoOld);
            }
            if (pedidoNew != null && !pedidoNew.equals(pedidoOld)) {
                pedidoNew.getMateriales().add(material);
                pedidoNew = em.merge(pedidoNew);
            }
            for (Rmc rmcListNewRmc : rmcListNew) {
                if (!rmcListOld.contains(rmcListNewRmc)) {
                    Material oldMaterialOfRmcListNewRmc = rmcListNewRmc.getMaterial();
                    rmcListNewRmc.setMaterial(material);
                    rmcListNewRmc = em.merge(rmcListNewRmc);
                    if (oldMaterialOfRmcListNewRmc != null && !oldMaterialOfRmcListNewRmc.equals(material)) {
                        oldMaterialOfRmcListNewRmc.getRmcList().remove(rmcListNewRmc);
                        oldMaterialOfRmcListNewRmc = em.merge(oldMaterialOfRmcListNewRmc);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = material.getId();
                if (findMaterial(id) == null) {
                    throw new NonexistentEntityException("The material with id " + id + " no longer exists.");
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
            Material material;
            try {
                material = em.getReference(Material.class, id);
                material.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The material with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Rmc> rmcListOrphanCheck = material.getRmcList();
            for (Rmc rmcListOrphanCheckRmc : rmcListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Material (" + material + ") cannot be destroyed since the Rmc " + rmcListOrphanCheckRmc + " in its rmcList field has a non-nullable material field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Pedido pedido = material.getPedido();
            if (pedido != null) {
                pedido.getMateriales().remove(material);
                pedido = em.merge(pedido);
            }
            em.remove(material);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Material> findMaterialEntities() {
        return findMaterialEntities(true, -1, -1);
    }

    public List<Material> findMaterialEntities(int maxResults, int firstResult) {
        return findMaterialEntities(false, maxResults, firstResult);
    }

    private List<Material> findMaterialEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Material.class));
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

    public Material findMaterial(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Material.class, id);
        } finally {
            em.close();
        }
    }

    public int getMaterialCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Material> rt = cq.from(Material.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
