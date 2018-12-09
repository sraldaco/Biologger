/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.usuario.controlador;

import com.biologger.modelo.Profesor;
import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.servicio.BCrypt;
import com.biologger.servicio.ImagenBase64;
import com.biologger.servicio.SMTP;
import com.biologger.usuario.modelo.ProfesorJpa;
import com.biologger.usuario.modelo.UsuarioJpa;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.mail.MessagingException;
import javax.servlet.http.Part;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="registro")
@ViewScoped
public class RegistroControlador {
    private Usuario usuario;
    private Profesor profe;
    private UsuarioJpa ujpa;
    private ProfesorJpa pjpa;
    private Date hoy;
    private String confirmacionContrasena;
    private String numeroTrabajador;
    private boolean chkProfesor;
    private Part file;

    public RegistroControlador() {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale("es-Mx"));
        this.hoy = new Date();
        this.usuario = new Usuario();
        this.profe = new Profesor();
        this.ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
        this.pjpa = new ProfesorJpa(UtilidadDePersistencia.getEntityManagerFactory());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getConfirmacionContrasena() {
        return confirmacionContrasena;
    }

    public void setConfirmacionContrasena(String confirmacionContrasena) {
        this.confirmacionContrasena = confirmacionContrasena;
    }

    public String getNumeroTrabajador() {
        return numeroTrabajador;
    }

    public void setNumeroTrabajador(String numeroTrabajador) {
        this.numeroTrabajador = numeroTrabajador;
    }

    public boolean isChkProfesor() {
        return chkProfesor;
    }

    public void setChkProfesor(boolean chkProfesor) {
        this.chkProfesor = chkProfesor;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    
    public void registrarUsuario() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            int m = 0;
            if (!usuario.getContrasena().equals(confirmacionContrasena)) { 
                current.addMessage(null,new FacesMessage(
                            FacesMessage.SEVERITY_WARN,"Error de contraseña", "La contraseña no coincide "
                                    + "con la confirmación."));
                m++;
            }
            if (chkProfesor) {
                profe = pjpa.buscarProfesorNumero(numeroTrabajador);
                if (profe != null) {
                    current.addMessage(null,new FacesMessage(
                             FacesMessage.SEVERITY_WARN,"# Trabajador", 
                             "El número de trabajador ya lo registró otro usuario, "
                                + "Si crees que hay un error puedes acudir con los "
                                + "administradores del sistema para hacer la aclaración. "
                                + "Mientras tanto puedes continuar tu registro como "
                                + "usuario normal."));
                    m++;
                }
                if ("".equals(numeroTrabajador)) {
                    current.addMessage(null,new FacesMessage(
                             FacesMessage.SEVERITY_INFO,"# Trabajador", 
                             "Debes ingresar tu número de trabajador para validar tus datos"));
                    m++;
                }
            }
            Usuario entidadUsuario;
            entidadUsuario = ujpa.buscarUsuarioNombreUsuario(usuario.getNombreUsuario());
            if(entidadUsuario != null) {
                current.addMessage(null,new FacesMessage(
                         FacesMessage.SEVERITY_ERROR,"Usuario no disponible", 
                         "El nombre de usuario que quieres usar ya está registrado, "
                                 + "ingresa otro nombre de usuario."));
                m++;
            }
            entidadUsuario = ujpa.buscarUsuarioCorreo(usuario.getCorreo());
            if(entidadUsuario != null) {
                current.addMessage(null,new FacesMessage(
                         FacesMessage.SEVERITY_ERROR,"Correo duplicado", 
                         "El correo ya está registrado, si eres el propietario "
                                 + "puedes restablecer tu contraseña"));
                m++;
            }
            if (m > 0) {
                return;
            }
            String codigoAleatorio = generarCodigoAleatorio();
            usuario.setContrasena(BCrypt.hashpw(confirmacionContrasena, BCrypt.gensalt()));
            usuario.setHashConfirmacion(BCrypt.hashpw(codigoAleatorio, BCrypt.gensalt()));
            usuario.setFechaRegistro(hoy);
            usuario.setUltimaActualizacion(hoy);
            usuario.setRol(3);
            usuario.setActivo(true);
            usuario.setCorreo(usuario.getCorreo().trim());
            enviarCodigoConfirmacion(codigoAleatorio);
            Flash flash = current.getExternalContext().getFlash();
            flash.setKeepMessages(true);
            ujpa.create(usuario);
            if (chkProfesor) {
                Profesor entidadProfesor = new Profesor();
                entidadProfesor.setNumero(numeroTrabajador);
                entidadProfesor.setValidado(false);
                entidadProfesor.setUsuario(usuario);
                pjpa.create(entidadProfesor);
                current.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,"Profesor", "Tu registro se ha realizado con éxito"
                            + " No obstante los premisos de profesor te serán otrorgados "
                            + "una vez que la solicitud sea validada."));
            }
            current.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,"Confirma tu correo",
                    usuario.getNombre() 
                    + " te hemos enviado un código de confirmación a " 
                    + usuario.getCorreo()));
        } catch (Exception ex) {
            current.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,"Error", ex.getMessage()));
        }
        current.getExternalContext().redirect("confirmar-correo.xhtml?correo=" + usuario.getCorreo());
    }
    
    private String generarCodigoAleatorio() {
        Random rnd = new Random();
        return Integer.toString(100000 + rnd.nextInt(900000));
    }
    
    private void enviarCodigoConfirmacion(String codigoAleatorio) 
            throws MessagingException, IOException {
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        String url = external.isSecure() ? "https://" : "http://";
        url += external.getRequestServerName();
        if (external.getRequestServerPort() != 80) {
            url += ":" + external.getRequestServerPort();
        }
        url += external.getApplicationContextPath();
        url += "/faces/usuario/confirmar-correo.xhtml?correo=";
        url+= usuario.getCorreo();
        String asunto = "Nuevo código de confirmación";
        String encabezado;
        encabezado = "te damos la bienvenida a Biologger";
        String cuerpoMensaje = String.join(
    	    System.getProperty("line.separator"),
    	    "<h1>",
            usuario.getNombre().trim(),
            encabezado,
            "</h1>",
    	    "<p>Usa el siguiente código para validar tu correo electrónico.</p>",
            "<p>El código tiene una vigencia de 24 horas.</p>",
            url,
            "<h2>",
            codigoAleatorio,
            "</h2>"
    	);
        try {
            SMTP.enviarCorreo(usuario.getCorreo(), asunto, cuerpoMensaje);
        } catch (MessagingException | IOException ex) {
            throw ex;
        }
    }
    
    public void subirImagen() throws IOException {
        FacesContext current = FacesContext.getCurrentInstance();
        try {
            String fotoCodificada = ImagenBase64.codificar(file);
            usuario.setFoto(fotoCodificada);
        } catch (IOException ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "¡Algo salio mal!",ex.getMessage()));
        }
    }
    
    public void eliminarImagen() {
        usuario.setFoto(null);
    }
}
