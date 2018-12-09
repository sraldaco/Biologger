/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.pedido.controlador;

import com.biologger.modelo.Material;
import com.biologger.modelo.Pedido;
import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.MaterialJpaController;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import com.biologger.pedido.modelo.PedidoJpa;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="carrito")
@SessionScoped
public class CarritoControlador {
    private List<Material> materiales;

    public CarritoControlador() {
        this.materiales = new ArrayList();
    }

    public List<Material> getMateriales() {
        return materiales;
    }
    
    public void agregarAlCarrito(Material material) {
        FacesContext current = FacesContext.getCurrentInstance();
        if (!material.getEstado().equals("Disponible")) {
            current.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,"No disponible",
                    material.getNombre() + " Ya no se encuentra disponible. No se agregó al carrito."));
            return;
        }
        if (!materiales.contains(material)) {
            materiales.add(material);
        }
        current.addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,"Carrito actualizado",
                material.getNombre() + " se ha agregado al carrito"));
    }
    
    public void eliminarDelCarrito(Material material) {
        materiales.remove(material);
        FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_INFO,"Carrito actualizado",
                material.getNombre() + " se ha eliminado del carrito"));
    }
    
    public void crearPedido(Usuario usuario) throws NonexistentEntityException, Exception {
        FacesContext current = FacesContext.getCurrentInstance();
        for (Pedido p : usuario.getPedidos()) {
            if (p.getEstado().equals("Vencido")){
                current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Préstamos bloqueados",
                "Tienes un préstamo vencido. No puedes solicitar más prestamos hasta que regreses los materiales."));
                return;
            }
        }
        PedidoJpa pjpa = new PedidoJpa(UtilidadDePersistencia.getEntityManagerFactory());
        Pedido pedido = new Pedido();
        pedido.setEstado("Pendiente");
        pedido.setFechaPedido(new Date());
        pedido.setUsuario(usuario);
        int i = 0;
        Iterator<Material> iterador = materiales.iterator();
        MaterialJpaController mjpa = new MaterialJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        while (iterador.hasNext()) {
            String estado = iterador.next().getEstado();
            if (!estado.equals("Disponible")){
                iterador.remove();
                i++;
            } 
        }
        if (!materiales.isEmpty()) {
            for (Material material : materiales) {
                material.setEstado("No disponible");
                try {
                    mjpa.edit(material);
                } catch (NonexistentEntityException ex) {
                    current.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_FATAL,"ERROR",ex.getMessage()));
                } catch (Exception ex) {
                    current.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_FATAL,"ERROR",ex.getMessage()));
                }
            }
            pedido.setMateriales(materiales);
            pjpa.create(pedido);
            materiales.clear();
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,"Pedido en progreso",
                "Tienes hasta tres días para ir por los materiales reservados, de lo contrario el pedido expirará "
                        + "y los materiales estarán disponibles automáticamente."));
            if (i > 0) {
                current.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,"Materiales no disponibles",
                        "No se han podido agregar " + i +" materiales al pedido porque ya no están disponibles."));
            }
            current.getExternalContext().redirect("mis-pedidos.xhtml");
        } else {
            current.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,"Pedido no procesado",
                "Al parecer no quedaron materiales disponibles que agregar al pedido, te los han ganado, por ese motivo no se ha podido completar el pedido."));
        }
    }
    
    public boolean seleccionado(Material material) {
        return materiales.contains(material);
    }
}
