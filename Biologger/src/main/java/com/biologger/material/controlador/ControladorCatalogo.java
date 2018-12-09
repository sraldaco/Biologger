/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.material.controlador;

import com.biologger.categoria.modelo.CategoriaJpa;
import com.biologger.material.modelo.MaterialJpa;
import com.biologger.modelo.Material;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.RmcJpaController;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author ikerlb
 */
@ManagedBean(name="catalogo")
@ViewScoped
public class ControladorCatalogo {

    private EntityManagerFactory emf;
    private List<Material> materiales;
    private MaterialJpa materialJPA;
    private RmcJpaController rmcJPA;
    private String buscar;
    private String categoriaId;
    private String nombre;
    private String descripcion;
    private String estado;
    private String modo;
    private String orden;
    private Integer maxResultados;
    private Integer totalResultados;
    private Integer pagina;
    private Integer totalPaginas;
    private CategoriaJpa categoriaJpa;

    
    
    public ControladorCatalogo(){
            this.emf = UtilidadDePersistencia.getEntityManagerFactory();
            this.rmcJPA = new RmcJpaController(emf);
            this.materialJPA = new MaterialJpa(emf);
            this.categoriaJpa = new CategoriaJpa(emf);
            this.materiales=new ArrayList<Material>();
            generarListaMateriales();
    }

    public Integer getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(Integer totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public Integer getMaxResultados() {
        return maxResultados;
    }

    public void setMaxResultados(Integer maxResultados) {
        this.maxResultados = maxResultados;
    }

    public List<Material> getMateriales() {
        return materiales;
    }

    public void setMateriales(List<Material> materiales) {
        this.materiales = materiales;
    }

    public Integer getTotalResultados() {
        return totalResultados;
    }

    public void setTotalResultados(Integer totalResultados) {
        this.totalResultados = totalResultados;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    public String getBuscar() {
        return buscar;
    }

    public void setBuscar(String buscar) {
        this.buscar = buscar;
    }
    
    
    public void generarListaMateriales(){
        Map<String,String> parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Map<String,String> cleanParams = new HashMap<String,String>();
        pagina = parametros.containsKey("pagina") && parametros.get("pagina") != null ? 
                parseInt(parametros.get("pagina")) : 1;
        maxResultados = parametros.containsKey("maxresultados") && parametros.get("maxresultados") != null ?
                parseInt(parametros.get("maxresultados")) : 12;
        buscar = parametros.containsKey("buscar") && parametros.get("buscar") != null ?
                parametros.get("buscar") : null;
        cleanParams.put("buscar", buscar);
        categoriaId = parametros.containsKey("categoria_id") && parametros.get("categoria_id") != null ?
                parametros.get("categoria_id") : null;
        cleanParams.put("categoria", categoriaId);
        /*
        nombre = parametros.containsKey("nombre") && parametros.get("nombre") != null ?
                parametros.get("nombre") : null;
        if (nombre != null) {
            cleanParams.put("nombre", nombre);
        }
        descripcion = parametros.containsKey("descripcion") && parametros.get("descripcion") != null ?
                parametros.get("descripcion") : null;
        if (descripcion!=null){
            cleanParams.put("descripcion",descripcion);
        }
        estado = parametros.containsKey("estado") && parametros.get("estado") != null ?
                parametros.get("estado") : null;
        if(estado != null) {
            cleanParams.put("estado",estado.equals("Disponible") ? "Disponible" : "No Disponible");
        }
         */
        orden = parametros.containsKey("orden") && parametros.get("orden") != null ?
                parametros.get("orden") : "id";
        modo = parametros.containsKey("modo") && parametros.get("modo") != null ?
                parametros.get("modo") : "ASC";
        //List<Rmc> rmcList = 
                //parametros.containsKey("categoria_id")&&parametros.get("categoria_id")!=null ? categoriaJpa.findCategoria(Integer.parseInt(parametros.get("categoria_id"))).getRmcList() : null;
        // totalResultados = materialJPA.countMaterialEntitiesFilter(cleanParams,rmcList);
        totalResultados = materialJPA.countMaterialEntitiesFilter(cleanParams);
        totalPaginas = (int) Math.ceil((float)totalResultados/(float)maxResultados);
        
        // materiales = materialJPA.findMaterialEntitiesFilter(cleanParams, maxResultados, (pagina -1)* maxResultados, orden, modo,rmcList);
        materiales = materialJPA.findMaterialEntitiesFilter(cleanParams, maxResultados, (pagina -1)* maxResultados, orden, modo);
    }
  
    
    public void filtrar() throws IOException {
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        String queryString = "?maxresultados=" + this.maxResultados;
        if (categoriaId != null) {
            queryString += "&categoria_id=" + categoriaId;
        }
        /*if (nombre != null) {
            queryString += "&nombre=" + nombre;
        }
        if(descripcion!=null){
            queryString+= "&descripcion="+descripcion;
        }
        if(estado != null) {
            queryString += "&estado=" + estado;
        }*/
        if (buscar !=null) {
            queryString += "&buscar=" + buscar;
        }
        if(orden != null) {
            queryString += "&orden=" + orden;
        }
        if(modo != null) {
            queryString += "&modo=" + modo;
        }
        external.redirect("lista.xhtml" + queryString);
    }    
   
}
