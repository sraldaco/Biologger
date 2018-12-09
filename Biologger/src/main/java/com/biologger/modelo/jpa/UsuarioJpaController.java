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
import com.biologger.modelo.Profesor;
import com.biologger.modelo.Kit;
import java.util.ArrayList;
import java.util.List;
import com.biologger.modelo.Pedido;
import com.biologger.modelo.Usuario;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author alex aldaco
 */
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) {
        if (usuario.getKits() == null) {
            usuario.setKits(new ArrayList<Kit>());
        }
        if (usuario.getPedidos() == null) {
            usuario.setPedidos(new ArrayList<Pedido>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Profesor profesor = usuario.getProfesor();
            if (profesor != null) {
                profesor = em.getReference(profesor.getClass(), profesor.getId());
                usuario.setProfesor(profesor);
            }
            List<Kit> attachedKits = new ArrayList<Kit>();
            for (Kit kitsKitToAttach : usuario.getKits()) {
                kitsKitToAttach = em.getReference(kitsKitToAttach.getClass(), kitsKitToAttach.getId());
                attachedKits.add(kitsKitToAttach);
            }
            usuario.setKits(attachedKits);
            List<Pedido> attachedPedidos = new ArrayList<Pedido>();
            for (Pedido pedidosPedidoToAttach : usuario.getPedidos()) {
                pedidosPedidoToAttach = em.getReference(pedidosPedidoToAttach.getClass(), pedidosPedidoToAttach.getId());
                attachedPedidos.add(pedidosPedidoToAttach);
            }
            usuario.setPedidos(attachedPedidos);
            em.persist(usuario);
            if (profesor != null) {
                Usuario oldUsuarioOfProfesor = profesor.getUsuario();
                if (oldUsuarioOfProfesor != null) {
                    oldUsuarioOfProfesor.setProfesor(null);
                    oldUsuarioOfProfesor = em.merge(oldUsuarioOfProfesor);
                }
                profesor.setUsuario(usuario);
                profesor = em.merge(profesor);
            }
            for (Kit kitsKit : usuario.getKits()) {
                Usuario oldProfesorOfKitsKit = kitsKit.getProfesor();
                kitsKit.setProfesor(usuario);
                kitsKit = em.merge(kitsKit);
                if (oldProfesorOfKitsKit != null) {
                    oldProfesorOfKitsKit.getKits().remove(kitsKit);
                    oldProfesorOfKitsKit = em.merge(oldProfesorOfKitsKit);
                }
            }
            for (Pedido pedidosPedido : usuario.getPedidos()) {
                Usuario oldUsuarioOfPedidosPedido = pedidosPedido.getUsuario();
                pedidosPedido.setUsuario(usuario);
                pedidosPedido = em.merge(pedidosPedido);
                if (oldUsuarioOfPedidosPedido != null) {
                    oldUsuarioOfPedidosPedido.getPedidos().remove(pedidosPedido);
                    oldUsuarioOfPedidosPedido = em.merge(oldUsuarioOfPedidosPedido);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuario usuario) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getId());
            Profesor profesorOld = persistentUsuario.getProfesor();
            Profesor profesorNew = usuario.getProfesor();
            List<Kit> kitsOld = persistentUsuario.getKits();
            List<Kit> kitsNew = usuario.getKits();
            List<Pedido> pedidosOld = persistentUsuario.getPedidos();
            List<Pedido> pedidosNew = usuario.getPedidos();
            List<String> illegalOrphanMessages = null;
            if (profesorOld != null && !profesorOld.equals(profesorNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Profesor " + profesorOld + " since its usuario field is not nullable.");
            }
            for (Kit kitsOldKit : kitsOld) {
                if (!kitsNew.contains(kitsOldKit)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Kit " + kitsOldKit + " since its profesor field is not nullable.");
                }
            }
            for (Pedido pedidosOldPedido : pedidosOld) {
                if (!pedidosNew.contains(pedidosOldPedido)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Pedido " + pedidosOldPedido + " since its usuario field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (profesorNew != null) {
                profesorNew = em.getReference(profesorNew.getClass(), profesorNew.getId());
                usuario.setProfesor(profesorNew);
            }
            List<Kit> attachedKitsNew = new ArrayList<Kit>();
            for (Kit kitsNewKitToAttach : kitsNew) {
                kitsNewKitToAttach = em.getReference(kitsNewKitToAttach.getClass(), kitsNewKitToAttach.getId());
                attachedKitsNew.add(kitsNewKitToAttach);
            }
            kitsNew = attachedKitsNew;
            usuario.setKits(kitsNew);
            List<Pedido> attachedPedidosNew = new ArrayList<Pedido>();
            for (Pedido pedidosNewPedidoToAttach : pedidosNew) {
                pedidosNewPedidoToAttach = em.getReference(pedidosNewPedidoToAttach.getClass(), pedidosNewPedidoToAttach.getId());
                attachedPedidosNew.add(pedidosNewPedidoToAttach);
            }
            pedidosNew = attachedPedidosNew;
            usuario.setPedidos(pedidosNew);
            usuario = em.merge(usuario);
            if (profesorNew != null && !profesorNew.equals(profesorOld)) {
                Usuario oldUsuarioOfProfesor = profesorNew.getUsuario();
                if (oldUsuarioOfProfesor != null) {
                    oldUsuarioOfProfesor.setProfesor(null);
                    oldUsuarioOfProfesor = em.merge(oldUsuarioOfProfesor);
                }
                profesorNew.setUsuario(usuario);
                profesorNew = em.merge(profesorNew);
            }
            for (Kit kitsNewKit : kitsNew) {
                if (!kitsOld.contains(kitsNewKit)) {
                    Usuario oldProfesorOfKitsNewKit = kitsNewKit.getProfesor();
                    kitsNewKit.setProfesor(usuario);
                    kitsNewKit = em.merge(kitsNewKit);
                    if (oldProfesorOfKitsNewKit != null && !oldProfesorOfKitsNewKit.equals(usuario)) {
                        oldProfesorOfKitsNewKit.getKits().remove(kitsNewKit);
                        oldProfesorOfKitsNewKit = em.merge(oldProfesorOfKitsNewKit);
                    }
                }
            }
            for (Pedido pedidosNewPedido : pedidosNew) {
                if (!pedidosOld.contains(pedidosNewPedido)) {
                    Usuario oldUsuarioOfPedidosNewPedido = pedidosNewPedido.getUsuario();
                    pedidosNewPedido.setUsuario(usuario);
                    pedidosNewPedido = em.merge(pedidosNewPedido);
                    if (oldUsuarioOfPedidosNewPedido != null && !oldUsuarioOfPedidosNewPedido.equals(usuario)) {
                        oldUsuarioOfPedidosNewPedido.getPedidos().remove(pedidosNewPedido);
                        oldUsuarioOfPedidosNewPedido = em.merge(oldUsuarioOfPedidosNewPedido);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = usuario.getId();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
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
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Profesor profesorOrphanCheck = usuario.getProfesor();
            if (profesorOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuario (" + usuario + ") cannot be destroyed since the Profesor " + profesorOrphanCheck + " in its profesor field has a non-nullable usuario field.");
            }
            List<Kit> kitsOrphanCheck = usuario.getKits();
            for (Kit kitsOrphanCheckKit : kitsOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuario (" + usuario + ") cannot be destroyed since the Kit " + kitsOrphanCheckKit + " in its kits field has a non-nullable profesor field.");
            }
            List<Pedido> pedidosOrphanCheck = usuario.getPedidos();
            for (Pedido pedidosOrphanCheckPedido : pedidosOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuario (" + usuario + ") cannot be destroyed since the Pedido " + pedidosOrphanCheckPedido + " in its pedidos field has a non-nullable usuario field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
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

    public Usuario findUsuario(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
