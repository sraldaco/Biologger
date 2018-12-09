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
import com.biologger.modelo.Material;
import com.biologger.modelo.Pedido;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class PedidoJpaController implements Serializable {

    public PedidoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pedido pedido) {
        if (pedido.getMateriales() == null) {
            pedido.setMateriales(new ArrayList<Material>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario usuario = pedido.getUsuario();
            if (usuario != null) {
                usuario = em.getReference(usuario.getClass(), usuario.getId());
                pedido.setUsuario(usuario);
            }
            List<Material> attachedMateriales = new ArrayList<Material>();
            for (Material materialesMaterialToAttach : pedido.getMateriales()) {
                materialesMaterialToAttach = em.getReference(materialesMaterialToAttach.getClass(), materialesMaterialToAttach.getId());
                attachedMateriales.add(materialesMaterialToAttach);
            }
            pedido.setMateriales(attachedMateriales);
            em.persist(pedido);
            if (usuario != null) {
                usuario.getPedidos().add(pedido);
                usuario = em.merge(usuario);
            }
            for (Material materialesMaterial : pedido.getMateriales()) {
                Pedido oldPedidoOfMaterialesMaterial = materialesMaterial.getPedido();
                materialesMaterial.setPedido(pedido);
                materialesMaterial = em.merge(materialesMaterial);
                if (oldPedidoOfMaterialesMaterial != null) {
                    oldPedidoOfMaterialesMaterial.getMateriales().remove(materialesMaterial);
                    oldPedidoOfMaterialesMaterial = em.merge(oldPedidoOfMaterialesMaterial);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pedido pedido) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido persistentPedido = em.find(Pedido.class, pedido.getId());
            Usuario usuarioOld = persistentPedido.getUsuario();
            Usuario usuarioNew = pedido.getUsuario();
            List<Material> materialesOld = persistentPedido.getMateriales();
            List<Material> materialesNew = pedido.getMateriales();
            if (usuarioNew != null) {
                usuarioNew = em.getReference(usuarioNew.getClass(), usuarioNew.getId());
                pedido.setUsuario(usuarioNew);
            }
            List<Material> attachedMaterialesNew = new ArrayList<Material>();
            for (Material materialesNewMaterialToAttach : materialesNew) {
                materialesNewMaterialToAttach = em.getReference(materialesNewMaterialToAttach.getClass(), materialesNewMaterialToAttach.getId());
                attachedMaterialesNew.add(materialesNewMaterialToAttach);
            }
            materialesNew = attachedMaterialesNew;
            pedido.setMateriales(materialesNew);
            pedido = em.merge(pedido);
            if (usuarioOld != null && !usuarioOld.equals(usuarioNew)) {
                usuarioOld.getPedidos().remove(pedido);
                usuarioOld = em.merge(usuarioOld);
            }
            if (usuarioNew != null && !usuarioNew.equals(usuarioOld)) {
                usuarioNew.getPedidos().add(pedido);
                usuarioNew = em.merge(usuarioNew);
            }
            for (Material materialesOldMaterial : materialesOld) {
                if (!materialesNew.contains(materialesOldMaterial)) {
                    materialesOldMaterial.setPedido(null);
                    materialesOldMaterial = em.merge(materialesOldMaterial);
                }
            }
            for (Material materialesNewMaterial : materialesNew) {
                if (!materialesOld.contains(materialesNewMaterial)) {
                    Pedido oldPedidoOfMaterialesNewMaterial = materialesNewMaterial.getPedido();
                    materialesNewMaterial.setPedido(pedido);
                    materialesNewMaterial = em.merge(materialesNewMaterial);
                    if (oldPedidoOfMaterialesNewMaterial != null && !oldPedidoOfMaterialesNewMaterial.equals(pedido)) {
                        oldPedidoOfMaterialesNewMaterial.getMateriales().remove(materialesNewMaterial);
                        oldPedidoOfMaterialesNewMaterial = em.merge(oldPedidoOfMaterialesNewMaterial);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = pedido.getId();
                if (findPedido(id) == null) {
                    throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.");
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
            Pedido pedido;
            try {
                pedido = em.getReference(Pedido.class, id);
                pedido.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.", enfe);
            }
            Usuario usuario = pedido.getUsuario();
            if (usuario != null) {
                usuario.getPedidos().remove(pedido);
                usuario = em.merge(usuario);
            }
            List<Material> materiales = pedido.getMateriales();
            for (Material materialesMaterial : materiales) {
                materialesMaterial.setPedido(null);
                materialesMaterial = em.merge(materialesMaterial);
            }
            em.remove(pedido);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Pedido> findPedidoEntities() {
        return findPedidoEntities(true, -1, -1);
    }

    public List<Pedido> findPedidoEntities(int maxResults, int firstResult) {
        return findPedidoEntities(false, maxResults, firstResult);
    }

    private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pedido.class));
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

    public Pedido findPedido(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pedido.class, id);
        } finally {
            em.close();
        }
    }

    public int getPedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pedido> rt = cq.from(Pedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
