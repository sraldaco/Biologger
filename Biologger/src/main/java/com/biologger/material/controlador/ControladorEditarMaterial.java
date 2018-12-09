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
import com.biologger.modelo.jpa.MaterialJpaController;
import com.biologger.modelo.jpa.RmcJpaController;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.Base64;
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
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ikerlb
 */
@ManagedBean(name="editarMaterial")
@ViewScoped
public class ControladorEditarMaterial {
    private EntityManagerFactory emf;
    private MaterialJpa materialJPA;
    private CategoriaJpaController categoriaJPA;
    private RmcJpaController rmcJPA;
    private List<Categoria> categorias;
    private Material material;
    private List<Categoria> categoriasSeleccionadas;
    private Part file;
    private String foto;
    private String nombre;
    private String descripcion;
    private String estado;
    
    //GET WORKING
    public ControladorEditarMaterial(){
        this.emf = UtilidadDePersistencia.getEntityManagerFactory();
        this.categoriaJPA = new CategoriaJpaController(emf);
        this.materialJPA = new MaterialJpa(emf);
        this.rmcJPA = new RmcJpaController(emf);
        this.categoriasSeleccionadas= new ArrayList<Categoria>();
        this.categorias = categoriaJPA.findCategoriaEntities();
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
                    this.material = mat;
                    this.nombre=mat.getNombre();
                    this.descripcion=mat.getDescripcion();
                    this.foto=mat.getFoto();
                    this.estado=mat.getEstado();
                    for(Rmc rmc:material.getRmcList()){
                        Categoria cat=categoriaJPA.findCategoria(rmc.getCategoria().getId());
                        categoriasSeleccionadas.add(cat);
                    }
                }
                else{
                    try {
                        current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error", "Id invalido"));
                        external.redirect("lista.xhtml");
                    }catch(IOException ex){
                        current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error", ex.getMessage()));
                    }
                    //INCORRECT ID! REDIRECT?
                } 

            }
            else{
                try{
                    current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error", "Parametro no reconocido"));
                    external.redirect("lista.xhtml");
                }catch(IOException ex){
                    current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error", ex.getMessage()));
                }
            }
        }
        else{
            try{
                current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error", "Parametro no reconocido"));                
                external.redirect("lista.xhtml");
            }catch(IOException ex){
                current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                        "Error", ex.getMessage()));
            }
        }

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
    
    public String getNombre(){
        return nombre;
    }
    
    public String getDescripcion(){
        return descripcion;
    }
    
    public void setNombre(String nombre){
        this.nombre=nombre;
    }
    
    public void setDescripcion(String descripcion){
        this.descripcion=descripcion;
    }    

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
    
    public void editarMaterial(){
        //remove all rmc from previous state:
        try{
            for(Rmc rmcOld:this.material.getRmcList()){
                rmcJPA.destroy(rmcOld.getId());
            }
        }catch(NonexistentEntityException e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error manejando categorias. Intentalo de nuevo"));
        }
        List<Rmc> rmcList=new ArrayList<Rmc>();
        for(Categoria cat:this.categoriasSeleccionadas){
            Rmc rmc=new Rmc();
            rmc.setCategoria(cat);
            rmc.setMaterial(this.material);
            rmcList.add(rmc);
        }//NO!!
        this.material.setNombre(this.nombre);
        this.material.setDescripcion(this.descripcion);
        this.material.setFoto(this.foto);
        this.material.setEstado(this.estado);
        this.material.setRmcList(rmcList);
        FacesContext current=FacesContext.getCurrentInstance();
        try{
            this.materialJPA.editarMaterial(this.material);
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Se ha editado el material"));
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.getExternalContext().redirect("ver.xhtml?id=" + this.material.getId());
        }catch(NonexistentEntityException | IOException e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Error editando material. Intentalo de nuevo"));
        }
    }
}
