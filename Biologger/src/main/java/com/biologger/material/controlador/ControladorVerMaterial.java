/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.material.controlador;

import com.biologger.material.modelo.MaterialJpa;
import com.biologger.modelo.Categoria;
import com.biologger.modelo.Material;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.CategoriaJpaController;
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
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ikerlb
 */
@ManagedBean(name="verMaterial")
@ViewScoped
public class ControladorVerMaterial {
    private EntityManagerFactory emf;
    private MaterialJpa materialJPA;
    private CategoriaJpaController categoriaJPA;
    private Material material;
    private List<Categoria> categorias;

    public ControladorVerMaterial() {
        this.emf = UtilidadDePersistencia.getEntityManagerFactory();
        this.materialJPA = new MaterialJpa(emf);
        this.categoriaJPA = new CategoriaJpaController(emf);
        this.categorias = new ArrayList<Categoria>();
        FacesContext current=FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        Map<String,String> parametros = external.getRequestParameterMap();
        String uri = ((HttpServletRequest) external.getRequest()).getRequestURI();
        String path = ((HttpServletRequest) external.getRequest()).getContextPath();
        if (!parametros.isEmpty()) {
            if (parametros.containsKey("id") && !parametros.get("id").isEmpty()) {
                int id = parseInt(parametros.get("id"));
                Material mat = materialJPA.findMaterial(id);
                if (mat != null) {
                    this.material=mat;
                    List<Rmc> rmcList=this.material.getRmcList();
                    for(Rmc rmc:rmcList){
                        Categoria cat=categoriaJPA.findCategoria(rmc.getCategoria().getId());
                        this.categorias.add(cat);
                    }
                }
                else{
                    try{
                        current.getExternalContext().redirect("lista.xhtml");
                    }catch(Exception e){
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error visualizando material. Intentalo de nuevo"));
                    }
                }
            }
            else{
                try{
                    current.getExternalContext().redirect("lista.xhtml");
                }catch(Exception e){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error visualizando material. Intentalo de nuevo"));
                }
                //throw message (redirect), incorrect parameters
            }
        }
        else{
            try{
                current.getExternalContext().redirect("lista.xhtml");
            }catch(Exception e){
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error visualizando material. Intentalo de nuevo"));
            }
            //throw message (redirect), no parameters
        }
        
    }
    
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
    
    
    
    
}
