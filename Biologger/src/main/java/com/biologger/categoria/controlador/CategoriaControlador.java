/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.categoria.controlador;

import com.biologger.categoria.modelo.CategoriaJpa;
import com.biologger.modelo.Categoria;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.RmcJpaController;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="beanCategoria")
@ViewScoped
public class CategoriaControlador {
    private List<Categoria> categoriasRoot;
    private List<Categoria> categorias;
    private Categoria categoria;
    private int padreId;
    private Categoria padre;
    private CategoriaJpa cjpa;
    private String nombre;

    public CategoriaControlador() {
        this.categoriasRoot = new ArrayList();
        this.categorias = new ArrayList();
        this.categoria = new Categoria();
        this.padre = new Categoria();
        cjpa = new CategoriaJpa(UtilidadDePersistencia.getEntityManagerFactory());
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        Map<String,String> parametros = external.getRequestParameterMap();
        String uri = ((HttpServletRequest) external.getRequest()).getRequestURI();
        String path = ((HttpServletRequest) external.getRequest()).getContextPath();
        if (!parametros.isEmpty()) {
            if (parametros.containsKey("padre") && !parametros.get("padre").isEmpty()){
                if (!parametros.get("padre").isEmpty()) {
                    int pid = parseInt(parametros.get("padre"));
                    setPadreId(pid);
                }
            } 
            if (parametros.containsKey("id") && !parametros.get("id").isEmpty()) {
                int id = parseInt(parametros.get("id"));
                Categoria cat = cjpa.findCategoria(id);
                if (cat != null) {
                    this.categoria = cat;
                    this.nombre = cat.getNombre();
                }
                if (uri.equals(path + "/faces/admin/categoria/editar.xhtml")) {
                    this.categorias = cjpa.getCategoriasEdit(categoria.getId());
                    if (categoria.getPadre() != null) {
                        this.padreId = categoria.getPadre().getId();
                    }
                } 
            }
        } 
        if (uri.equals(path + "/faces/admin/categoria/agregar.xhtml")) {
            this.categorias = cjpa.findCategoriaEntities();
        }
    }

    public List<Categoria> getCategoriasRoot() {
        this.categoriasRoot = cjpa.getCategoriasRoot();
        return categoriasRoot;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    
    public int getPadreId() {
        return padreId;
    }

    public void setPadreId(int padreId) {
        this.padreId = padreId;
    }
    
    public void crearCategoria() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        if (padreId != 0) {
            padre = cjpa.findCategoria(padreId);
            categoria.setPadre(padre);
        }
        Categoria entidadCategoria = cjpa.buscarCategoriaNombre(categoria.getNombre());
        if (entidadCategoria != null) {
            current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,"Error",
                categoria.getNombre() + " ya está en uso. Por favor, elige otro nombre."));
            return;
        }
        cjpa.create(categoria);
        Flash flash = current.getExternalContext().getFlash();
        flash.setKeepMessages(true);
        current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,"Nueva categoría",
                "La " + categoria.getNombre() + " ha sido guardada correctamente"));
        current.getExternalContext().redirect("lista.xhtml");
    }
    
    public void editarCategoria(){
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            if (padreId != 0) {
                padre = cjpa.findCategoria(padreId);
                categoria.setPadre(padre);
            } else {
                categoria.setPadre(null);
            }
            if (!nombre.equals(categoria.getNombre())) {
                Categoria entidadCategoria = cjpa.buscarCategoriaNombre(categoria.getNombre());
                if (entidadCategoria != null) {
                    current.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,"Error",
                        categoria.getNombre() + " ya está en uso. Por favor, elige otro nombre."));
                    return;
                }
            }
            cjpa.edit(categoria);
            String nombre = categoria.getNombre();
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,"Categoría editada",
                    "La categoría " + nombre + " ha sido editada correctamente"));
            current.getExternalContext().redirect("lista.xhtml");
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,"Error",
                ex.getMessage()));
        } catch (Exception ex) {
            current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,"Error",
                ex.getMessage()));
        }
    }
    
    public void eliminarCategoria() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            String nombre = categoria.getNombre();
            if (!categoria.getRmcList().isEmpty()) {
                RmcJpaController rmcjpa = 
                        new RmcJpaController(UtilidadDePersistencia.getEntityManagerFactory());
                for (Rmc rmc : categoria.getRmcList()) {
                    rmcjpa.destroy(rmc.getId());
                }
            }
            cjpa.destroy(categoria.getId());
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,"Categoría eliminada",
                    "La " + nombre + " ha sido eliminada correctamente"));
            current.getExternalContext().redirect("lista.xhtml");
        } catch (IllegalOrphanException ex) {
            current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,"Error",
                ex.getMessage()));
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,"Error",
                ex.getMessage()));
        }
    }
}
