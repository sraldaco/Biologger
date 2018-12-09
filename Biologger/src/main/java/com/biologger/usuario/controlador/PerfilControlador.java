/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.usuario.controlador;

import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import com.biologger.servicio.BCrypt;
import com.biologger.servicio.ImagenBase64;
import com.biologger.usuario.modelo.UsuarioJpa;
import java.io.IOException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.Part;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="perfil")
@ViewScoped
public class PerfilControlador {
    private String contrasenaActual;
    private String contrasena;
    private String confirmacionContrasena;
    private Usuario usuario;
    private UsuarioJpa ujpa;
    private Part file;
    
    public PerfilControlador() {
        this.ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
        Usuario entidadUsuario = (Usuario) FacesContext.getCurrentInstance()
                                                        .getExternalContext()
                                                        .getSessionMap().get("usuario");
        if (entidadUsuario != null) {
            this.usuario = ujpa.findUsuario(entidadUsuario.getId());
        }
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        if (!contrasenaActual.equals("")) {
            this.contrasenaActual = contrasenaActual;
        } else {
            this.contrasenaActual = null;
        }
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        if (!contrasena.equals("")) {
            this.contrasena = contrasena;
        } else {
            this.contrasena = null;
        }
    }

    public String getConfirmacionContrasena() {
        return confirmacionContrasena;
    }

    public void setConfirmacionContrasena(String confirmacionContrasena) {
        if (!confirmacionContrasena.equals("")) {
            this.confirmacionContrasena = confirmacionContrasena;
        } else {
            this.confirmacionContrasena = null;
        }
    }
    
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
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
    
    public void editarUsuario() {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            if (contrasena != null || confirmacionContrasena != null) {
                if (contrasenaActual == null) {
                   current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ingresa tu contraseña","Para hacer cambios en tu contraseña, debes ingresar tu contraseña actual."));
                   return;
                } else if (!BCrypt.checkpw(contrasenaActual, usuario.getContrasena())) {
                    current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Contraseña actual no coincide","Para hacer cambios en tu contraseña, debes ingresar tu contraseña actual."));
                   return;
                }
                if (!contrasena.equals(confirmacionContrasena)) {
                    current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "La nueva contraseña no coincide","La nueva contraseña debe ser igual a la confirmación de contraseña."));
                   return;
                } else {
                    usuario.setContrasena(BCrypt.hashpw(contrasena, BCrypt.gensalt()));
                }
            }
            ujpa.edit(usuario);
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Cambios guardados","Los datos de tu cuenta usuario han sido actualizados con éxito"));
            current.getExternalContext().redirect("ver.xhtml");
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "El usuario no existe","El usuario que deseas eliminar ya no existe en la base de datos."));
        } catch (Exception ex) {
            current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "Error", ex.getMessage()));
        }
    }
}
