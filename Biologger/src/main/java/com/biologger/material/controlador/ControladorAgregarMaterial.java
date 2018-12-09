package com.biologger.material.controlador;

import com.biologger.modelo.Material;
import com.biologger.modelo.Categoria;
import com.biologger.modelo.Rmc;
import com.biologger.modelo.jpa.MaterialJpaController;
import com.biologger.modelo.jpa.CategoriaJpaController;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.RmcJpaController;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ikerlb
 */
@ManagedBean(name="agregaMat")
@ViewScoped
public class ControladorAgregarMaterial {
    
    private EntityManagerFactory emf;
    private MaterialJpaController materialJPA;
    private CategoriaJpaController categoriaJPA;
    private RmcJpaController rmcJPA;
    private List<Categoria> categorias;
    private Material material;
    private List<Categoria> categoriasSeleccionadas;
    private Part file;
    private String foto;
    
    public ControladorAgregarMaterial(){
        this.emf = UtilidadDePersistencia.getEntityManagerFactory();
        this.categoriaJPA = new CategoriaJpaController(emf);
        this.materialJPA = new MaterialJpaController(emf);
        this.rmcJPA = new RmcJpaController(emf);
        this.material = new Material();
        this.categoriasSeleccionadas= new ArrayList<Categoria>();
        this.categorias = categoriaJPA.findCategoriaEntities();
    }
    
    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public List<Categoria> getCategoriasSeleccionadas() {
        return categoriasSeleccionadas;
    }

    public void setCategoriasSeleccionadas(List<Categoria> categoriasSeleccionadas) {
        this.categoriasSeleccionadas = categoriasSeleccionadas;
    }
    
    public Part getFile() {
        return file;
    }
 
    public void setFile(Part file) {
        this.file = file;
    }
    
    
    public void handleUpload() {
        InputStream input = null;
        try {
            input = file.getInputStream();
            byte[] bytes = IOUtils.toByteArray(input);
            String base64Encoded = "data:image/png;base64,";
            base64Encoded += Base64.getEncoder().encodeToString(bytes);
            foto=base64Encoded;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Se ha cargado la imagen!"));
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Hubo un error con la imagen"));
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Hubo un error con la imagen"));
            }
        }        
    }
    
    public void eliminarImagen() {
        foto=null;
        file=null;
    }
    
    public void crearMateriales(){
        material.setFoto(foto);
        try{
            materialJPA.create(material);
            try{
                for(Categoria cat : categoriasSeleccionadas){
                    Rmc rmc=new Rmc();
                    rmc.setCategoria(cat);
                    rmc.setMaterial(material);
                    rmcJPA.create(rmc);
                }
                FacesContext current=FacesContext.getCurrentInstance();
                Flash flash = current.getExternalContext().getFlash();
                flash.setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Se ha creado el material"));
                current.getExternalContext().redirect("ver.xhtml?id=" + this.material.getId());
            }catch(Exception e){
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error vinculando material con categorias. Intenta editarlo."));
            }
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error creando material. Intentalo de nuevo"));
        }
        
        
    }
    
}
