/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.kit.controlador;

import com.biologger.modelo.Enlace;
import com.biologger.modelo.Kit;
import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.EnlaceJpaController;
import com.biologger.modelo.jpa.KitJpaController;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;


/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="beanKit")
@ViewScoped
public class KitControlador {
    private int idKit;
    private String titulo;
    private String url;
    private List<Enlace> enlacesRemove;
    private Kit kit;
    private KitJpaController kjpa;
    
    public KitControlador() {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        this.enlacesRemove = new ArrayList();
        this.kit = new Kit();
        this.kjpa = new KitJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        Map<String,String> p = external.getRequestParameterMap();
        if (p.containsKey("id") && p.get("id") != null) {
            kit = kjpa.findKit(Integer.parseInt(p.get("id")));
        }
    }

    public int getIdKit() {
        return idKit;
    }

    public void setIdKit(int idKit) {
        this.idKit = idKit;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }
    
    public void crear(Usuario usuario) throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        kit.setProfesor(usuario);
        kjpa.create(kit);
        external.getFlash().setKeepMessages(true);
        current.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,"Nuevo kit",
                        "Se ha creado un nuevo kit" + kit.getTitulo()));
        external.redirect("ver.xhtml?id=" + kit.getId());
    }
    
    public void editar() throws Exception {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        EnlaceJpaController ejpa = new EnlaceJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        try {
            for (Enlace e : enlacesRemove) {
                ejpa.destroy(e.getId());
            }
            for (Enlace e : kit.getEnlaces()) {
                if (e.hashCode() == 0 && !e.getUrl().equals("")) {
                    String contextPath = external.getRequestContextPath();
                    if (e.getUrl().contains(contextPath)) {
                        String[] uri = e.getUrl().split(contextPath);
                        e.setUrl(uri[1]);
                    }
                    e.setKit(kit);
                    ejpa.create(e);
                } else {
                    ejpa.edit(e);
                }
            }
            kjpa.edit(kit);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(KitControlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(KitControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
        external.getFlash().setKeepMessages(true);
        current.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,"Nuevo kit",
                        "Se ha editado el kit: " + kit.getTitulo()));
        external.redirect("ver.xhtml?id=" + kit.getId());
    }
    
    public void eliminar() throws Exception {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        EnlaceJpaController ejpa = new EnlaceJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        try {
            String titulo = kit.getTitulo();
            for (Enlace e : kit.getEnlaces()) {
                ejpa.destroy(e.getId());
            }
            kjpa.destroy(kit.getId());
            external.getFlash().setKeepMessages(true);
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"kit eliminado",
            "El kit " + titulo + " ha sido eliminado"));
            external.redirect("mis-kits.xhtml");
        } catch (IllegalOrphanException ex) {
            Logger.getLogger(KitControlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(KitControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void eliminarDeKit(Enlace e, int index) {
        if (e.hashCode() > 0) {
            enlacesRemove.add(e);
        }
        kit.getEnlaces().remove(index);
    }
    
    public void agregarEnlace() {
        Enlace e = new Enlace();
        e.setKit(kit);
        kit.getEnlaces().add(e);
    }
    
    public void agregarAKit() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        Kit k = kjpa.findKit(idKit);
        for (Enlace e : k.getEnlaces()) {
            if (e.getUrl().equals(url)) {
                current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Enlace duplicado",
                        "Este kit ya contiene este enlace con el título " + e.getTitulo() +", no se volvió a agregar"));
                return;
            }
        }
        EnlaceJpaController ejpa = new EnlaceJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        Enlace link = new Enlace();
        link.setKit(k);
        link.setTitulo(titulo);
        link.setUrl(url);
        ejpa.create(link);
        external.getFlash().setKeepMessages(true);
        current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Enlace añadido",
                "El enlace " + link.getTitulo() + " se ha añadido al kit " + k.getTitulo()));
        String contextPath = external.getRequestContextPath();
        external.redirect(contextPath + url);
    }
}
