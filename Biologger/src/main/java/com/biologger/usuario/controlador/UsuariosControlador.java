/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.usuario.controlador;

import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.exceptions.IllegalOrphanException;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import com.biologger.servicio.ImagenBase64;
import com.biologger.usuario.modelo.ProfesorJpa;
import com.biologger.usuario.modelo.UsuarioJpa;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="beanUsuarios")
@ViewScoped
public class UsuariosControlador {
    private List<Usuario> usuarios;
    private Usuario usuario;
    private int maxResultados;
    private int pagina;
    private int totalPaginas;
    private int totalResultados;
    private int rol;
    private String nombre;
    private String modo;
    private String orden;
    private Boolean activo;
    private String estado;
    private UsuarioJpa ujpa;
    private Part file;

    public UsuariosControlador() {
        this.usuario = new Usuario();
        this.ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
        this.usuarios = new ArrayList();
        String uri = ((HttpServletRequest) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequest()).getRequestURI();
        String path = ((HttpServletRequest) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequest()).getContextPath();
        if (uri.equals(path + "/faces/admin/usuario/lista-de-usuarios.xhtml")) {
            generarListaUsuarios();
        } else {
            cargarUsuario();
        }
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getTotalResultados() {
        return totalResultados;
    }

    public void setTotalResultados(int totalResultados) {
        this.totalResultados = totalResultados;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public int getMaxResultados() {
        return maxResultados;
    }
    
    public void setMaxResultados(int maxResultados) {
        this.maxResultados = maxResultados;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if ("".equals(estado)) {
            this.estado = null;
        } else  {
            this.estado = estado;
        }
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if ("".equals(nombre)) {
            this.nombre = null;
        } else  {
            this.nombre = nombre;
        }
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
    
    public void cargarUsuario(){
        String id = null;
        Map<String,String> parametros = 
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (parametros.containsKey("id") && parametros.get("id") != null) {
            id = parametros.get("id");
        }
        Usuario entidadUsuario = ujpa.findUsuario(Integer.parseInt(id));
        if (!Objects.equals(entidadUsuario.getId(), usuario.getId())) {
            usuario = entidadUsuario;
        }
    }
    
    public void editarUsuario() {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            ujpa.edit(usuario);
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Cambios guardados","Los datos del usuario " + usuario.getNombre() + 
                        " han sido actualizados con éxito"));
            current.getExternalContext().redirect("ver.xhtml?id=" + usuario.getId());
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "El usuario no existe","El usuario que deseas eliminar ya no existe en la base de datos."));
        } catch (Exception ex) {
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "Error", ex.getMessage()));
        }
    }
    
    public void subirImagen() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            String fotoCodificada = ImagenBase64.codificar(file);
            usuario.setFoto(fotoCodificada);
            setUsuario(usuario);
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Imagen cargada","La foto del usuario " + usuario.getNombre() + 
                        " ha sido cargada con éxito, para preservar los cambios, de click en el boton guardar."));
        } catch (Exception ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "¡Algo salio mal!",ex.getMessage()));
        }
    }
    
    public void generarListaUsuarios() {
        Map<String,String> parametros = 
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Map<String,String> cleanParams = new HashMap<String,String>();
        pagina = parametros.containsKey("pagina") && parametros.get("pagina") != null ? 
                parseInt(parametros.get("pagina")) : 1;
        maxResultados = parametros.containsKey("maxresultados") && parametros.get("maxresultados") != null ?
                parseInt(parametros.get("maxresultados")) : 25;
        nombre = parametros.containsKey("nombre") && parametros.get("nombre") != null ?
                parametros.get("nombre") : null;
        if (nombre != null) {
            cleanParams.put("nombre", nombre);
        }
        rol =  parametros.containsKey("rol") && parametros.get("rol") != null ?
                parseInt(parametros.get("rol")) : -1;
        if (rol > 0) {
            cleanParams.put("rol", Integer.toString(rol));
        }
        estado = parametros.containsKey("estado") && parametros.get("estado") != null ?
                 parametros.get("estado") : null;
        activo = estado != null ? getEstadoBool(estado) : null;
        if(activo != null) {
            cleanParams.put("activo",activo ? "true" : "false");
        }
        orden = parametros.containsKey("orden") && parametros.get("orden") != null ?
                parametros.get("orden") : "id";
        modo = parametros.containsKey("modo") && parametros.get("modo") != null ?
                parametros.get("modo") : "ASC";
        totalResultados = ujpa.countUsuarioEntitiesFilter(cleanParams);
        totalPaginas = (int) Math.ceil((float)totalResultados/(float)maxResultados);
        usuarios = ujpa.findUsuarioEntitiesFilter(cleanParams, maxResultados, (pagina -1)* maxResultados, orden, modo);
    }
    
    private Boolean getEstadoBool(String estado) {
        if (estado != null && !estado.equals("")) {
            switch (estado) {
                case "true" :
                    return true;
                case "false" :
                    return false;
                default: break;
            }
        }
        return null;
    }
    
    public void eliminarUsuario() throws IllegalOrphanException, NonexistentEntityException, IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            String nombre = usuario.getNombre();
            if(usuario.getProfesor() != null) {
               ProfesorJpa pjpa = new ProfesorJpa(UtilidadDePersistencia.getEntityManagerFactory());
               pjpa.destroy(usuario.getProfesor().getId());
            }
            ujpa.destroy(usuario.getId());
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "¡Usuario eliminado!", "El usuario " + nombre + " ha sido eliminado con éxito"));
            current.getExternalContext().redirect("lista-de-usuarios.xhtml");
        } catch (IllegalOrphanException ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "¡Algo salio mal!",ex.getMessage()));
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "¡Algo salio mal!",ex.getMessage()));
        }
    }
    
    public void filtrar() throws IOException {
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        String queryString = "?maxresultados=" + this.maxResultados;
        if (nombre != null) {
            queryString += "&nombre=" + nombre;
        }
        if(rol > 0) {
            queryString += "&rol=" + rol;
        }
        if(estado != null) {
            queryString += "&estado=" + estado;
        }
        if(orden != null) {
            queryString += "&orden=" + orden;
        }
        if(modo != null) {
            queryString += "&modo=" + modo;
        }
        external.redirect("lista-de-usuarios.xhtml" + queryString);
    }
}
