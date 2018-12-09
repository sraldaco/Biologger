package com.biologger.material.controlador;

import com.biologger.modelo.Material;
import com.biologger.modelo.Categoria;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.jpa.MaterialJpaController;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.RmcJpaController;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManagerFactory;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ikerlb
 */
@ManagedBean(name="mats")
@ViewScoped
public class ControladorMateriales {
    
    private Integer numMateriales;
    private EntityManagerFactory emf;
    private List<Material> materiales;
    private MaterialJpaController materialJPA;
    private RmcJpaController rmcJPA;
    private Integer pagina;
    private Integer maximaPagina;
    
    public ControladorMateriales(){
        this.emf = UtilidadDePersistencia.getEntityManagerFactory();
        this.rmcJPA = new RmcJpaController(emf);
        this.materialJPA = new MaterialJpaController(emf);
        this.pagina=1;
        this.numMateriales=12;
        this.maximaPagina=(int)Math.ceil(materialJPA.getMaterialCount()/((double)this.numMateriales));
        //zero indexed?
        this.materiales = materialJPA.findMaterialEntities(this.numMateriales,(this.pagina-1)*this.numMateriales);

    }

    public Integer getNumMateriales() {
        return numMateriales;
    }

    public void setNumMateriales(Integer numMateriales) {
        this.numMateriales = numMateriales;
    }
    
    public List<Material> getMateriales() {
        return materiales;
    }
    
    public void setMateriales(List<Material> materiales) {
        this.materiales = materiales;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }
    
    public Integer getMaximaPagina() {
        return maximaPagina;
    }

    public void setMaximaPagina(Integer maximaPagina) {
        this.maximaPagina = maximaPagina;
    }
    
    public void paginaAnterior(){
        cambiarPagina(this.pagina-1);
    }
    
    public void paginaSiguiente(){
        cambiarPagina(this.pagina+1);
    }
    
    public void primerPagina(){
        cambiarPagina(1);
    }
    
    public void ultimaPagina(){
        cambiarPagina(this.maximaPagina);
    }
    
    public void cambiarPagina(Integer nuevaPagina){
        this.pagina=nuevaPagina;
        this.materiales = materialJPA.findMaterialEntities(this.numMateriales,(this.pagina-1)*this.numMateriales);
    }
    
    
    public void eliminarMateriales(Material material) {
        try {
            List<Rmc> rmcList=material.getRmcList();
            for(Rmc r: rmcList){
                rmcJPA.destroy(r.getId());
            }
            materialJPA.destroy(material.getId());
            this.materiales = materialJPA.findMaterialEntities(this.numMateriales,(this.pagina-1)*this.numMateriales);
            this.maximaPagina=(int)Math.ceil(materialJPA.getMaterialCount()/((double)this.numMateriales));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Eliminado","Se elimino el material"));
        } catch (NonexistentEntityException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error",e.getMessage()));
        } catch (IllegalOrphanException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","No se pudo eliminar material"));
            Logger.getLogger(ControladorMateriales.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
