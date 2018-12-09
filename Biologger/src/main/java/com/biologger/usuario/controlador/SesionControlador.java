/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.usuario.controlador;

import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.servicio.BCrypt;
import com.biologger.usuario.modelo.UsuarioJpa;
import java.io.IOException;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="sesion")
@RequestScoped
public class SesionControlador {
    private Date hoy;
    private String nombreUsuario;
    private String contrasena;
    private UsuarioJpa ujpa;

    public SesionControlador() {
        hoy = new Date();
        this.ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public void iniciarSesion() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        Usuario entidadUsuario = ujpa.buscarUsuarioLogin(nombreUsuario);
        try {
            if (entidadUsuario == null) {
                current.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,"Advertencia","El nombre de usuario "
                                + "no está registrado en la base de datos."));
                return;
            }
            if (entidadUsuario.getUltimoAcceso() == null) {
                Flash flash = external.getFlash();
                flash.setKeepMessages(true);
                current.addMessage(null,new FacesMessage(
                                FacesMessage.SEVERITY_WARN,"Confirma tu correo",
                                    "Para iniciar sesión primero debes confirmar tu correo electrónico"));
                external.redirect("confirmar-correo.xhtml");
            }
            if (!entidadUsuario.getActivo()) {
                current.addMessage(null,new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,"Cuenta inactiva","Tu cuenta está inactiva, "
                                        + "pasa con los administradores del sistema para mayor información"
                                        + " acerca del bloqueo de tu cuenta."));
                return;
            }
            if (!BCrypt.checkpw(contrasena, entidadUsuario.getContrasena())) {
                current.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,"Error","La contraseña no es correcta."));
                return;
            }
            entidadUsuario.setUltimoAcceso(hoy);
            ujpa.edit(entidadUsuario);
        } catch (Exception ex) {
            current.addMessage(null,new FacesMessage(
                            FacesMessage.SEVERITY_FATAL,"¡Algo salió mal!",ex.getMessage()));
        }
        current.getExternalContext().getSessionMap()
                .put("usuario", entidadUsuario);
        Flash flash = external.getFlash();
        flash.setKeepMessages(true);
        current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Bienvenido",
                            entidadUsuario.getNombre() + " has iniciado sesión correctamente"));
        external.redirect(external.getRequestContextPath() + "/faces/index.xhtml");
    }
    
    public void cerrarSesion() throws IOException{
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        Object sesion = external.getSessionMap().get("usuario");
        Flash flash = external.getFlash();
        flash.setKeepMessages(true);
        String nombre = getUsuario().getNombre();
        current.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gracias por visitarnos",
                            nombre + " has cerrado sesión correctamente, vuelve pronto"));
        external.redirect(external.getRequestContextPath() + "/faces/usuario/iniciar-sesion.xhtml");
        if(sesion != null && sesion.getClass() == Usuario.class) {
            external.invalidateSession();
        }
    }
    
    public Usuario getUsuario() {
        FacesContext current = FacesContext.getCurrentInstance();
        Usuario entidadUsuario = null;
        ExternalContext external = current.getExternalContext();
        Object sesion = external.getSessionMap().get("usuario");
        if(sesion != null && sesion.getClass() == Usuario.class) {
            entidadUsuario = (Usuario) external.getSessionMap().get("usuario");
            entidadUsuario = ujpa.findUsuario(entidadUsuario.getId());
        }
        if (entidadUsuario == null) {
            entidadUsuario = new Usuario();
            entidadUsuario.setId(-1);
            entidadUsuario.setNombre("Anónimo");
            entidadUsuario.setCorreo("No conectado");
            entidadUsuario.setNombreUsuario("Anónimo");
        }
        if (entidadUsuario.getFoto() == null) {
            entidadUsuario.setFoto("/resources/assets/images/user/icon.png");
        }
        return entidadUsuario;
    }
}
