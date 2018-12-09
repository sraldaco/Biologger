/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.pedido.controlador;

import com.biologger.modelo.Material;
import com.biologger.modelo.Pedido;
import com.biologger.modelo.Usuario;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.MaterialJpaController;
import com.biologger.modelo.jpa.exceptions.NonexistentEntityException;
import com.biologger.pedido.modelo.PedidoJpa;
import com.biologger.usuario.modelo.UsuarioJpa;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="pedidoBean")
@ViewScoped
public class PedidoControlador {
    private String estado;
    private String correo;
    private int maxResultados;
    private int pagina;
    private int totalPaginas;
    private int totalResultados;
    private String modo;
    private String orden;
    private Pedido pedido;
    private List<Pedido> pedidos;
    private PedidoJpa pjpa;
    private Date hoy;
    private int id;
    
    public PedidoControlador() {
        this.pedido = new Pedido();
        this.pjpa = new PedidoJpa(UtilidadDePersistencia.getEntityManagerFactory());
        this.hoy = new Date();
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        Map<String,String> p = external.getRequestParameterMap();
        this.id = p.containsKey("pedido_id") && p.get("pedido_id") != null ?
                parseInt(p.get("pedido_id")) : 0;
        if (id > 0) {
            cargarPedido(id);
        }
        String uri = ((HttpServletRequest) external.getRequest()).getRequestURI();
        String path = ((HttpServletRequest) external.getRequest()).getContextPath();
        if (uri.equals(path + "/faces/admin/pedido/lista.xhtml")) {
            this.estado = p.containsKey("estado") && p.get("estado") != null ?
                    p.get("estado") : null;
            this.correo = p.containsKey("correo") && p.get("correo") != null ?
                    p.get("correo") : null;
            this.pagina = p.containsKey("pagina") && p.get("pagina") != null ? 
                parseInt(p.get("pagina")) : 1;
            this.maxResultados = p.containsKey("maxresultados") && p.get("maxresultados") != null ?
                    parseInt(p.get("maxresultados")) : 10;
            this.orden = p.containsKey("orden") && p.get("orden") != null ?
                    p.get("orden") : "id";
            this.modo = p.containsKey("modo") && p.get("modo") != null ?
                    p.get("modo") : "ASC";
            this.totalResultados = pjpa.countfiltrarPedidos(estado,correo);
            this.totalPaginas = (int) Math.ceil((float)totalResultados/(float)maxResultados) > 0 ?
                    (int) Math.ceil((float)totalResultados/(float)maxResultados) : 1;
            this.pedidos = pjpa.filtrarPedidos(estado,correo, maxResultados, (pagina -1)* maxResultados, orden, modo);
        } 
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if ("".equals(correo)) {
            this.correo = null;
        } else  {
            this.correo = correo;
        }
    }
    
    public int getMaxResultados() {
        return maxResultados;
    }

    public void setMaxResultados(int maxResultados) {
        this.maxResultados = maxResultados;
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

    public int getTotalResultados() {
        return totalResultados;
    }

    public void setTotalResultados(int totalResultados) {
        this.totalResultados = totalResultados;
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
    
    public void cancelar(Pedido pedido) {
        try {
            cancelar(pedido,true);
        } catch (Exception ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void cancelar(Pedido pedido, boolean inBean) throws NonexistentEntityException, Exception {
        FacesContext current = FacesContext.getCurrentInstance();
        MaterialJpaController mjpa = new MaterialJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        if ((pedido.getEstado().equals("Activo") || pedido.getEstado().equals("Vencido")) && inBean) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,
                        "Error","No se puede cancelar este préstamo."));
            return;
        }
        try {
            List<Material> materiales = pedido.getMateriales();
            for (Material material : materiales) {
                material.setEstado("Disponible");
                mjpa.edit(material);
            }
            int id = pedido.getId();
            pjpa.destroy(id);
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO,"Pedido cancelado",
                            "El pedido #" + id + " ha sido cancelado."));
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,"Error",ex.getMessage()));
        } catch (Exception ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,"Error",ex.getMessage()));
        }
    }
    
    public void eliminar() throws NonexistentEntityException, Exception {
        FacesContext current = FacesContext.getCurrentInstance();
        ExternalContext external = current.getExternalContext();
        MaterialJpaController mjpa = new MaterialJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        try {
            List<Material> materiales = pedido.getMateriales();
            for (Material material : materiales) {
                material.setEstado("Disponible");
                mjpa.edit(material);
            }
            int id = pedido.getId();
            pjpa.destroy(id);
            external.getFlash().setKeepMessages(true);
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO,"Pedido eliminado",
                            "El pedido #" + id + " ha sido eliminado."));
            external.redirect("lista.xhtml");
        } catch (NonexistentEntityException ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,"Error",ex.getMessage()));
        } catch (Exception ex) {
            current.addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,"Error",ex.getMessage()));
        }
    }
    
    public Date expiraInfo(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.add(Calendar.DAY_OF_YEAR, 3);
        return calendario.getTime();
    }
    
    private void cargarPedido(int id) {
        Pedido p = new Pedido();
        p = pjpa.findPedido(id);
        if (p != null) {
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(hoy);
            calendario.add(Calendar.DAY_OF_YEAR, 5);
            Date fechaEntrega = calendario.getTime();
            p.setFechaDespacho(hoy);
            p.setFechaEntrega(fechaEntrega);
            setPedido(p);
        }
    }
    
    public void prepararDespacho(Pedido pedido) throws IOException {
        String uri =  "procesar.xhtml?pedido_id=" + pedido.getId();
        FacesContext.getCurrentInstance().getExternalContext().redirect(uri);
    }
    
    public void quitarDelPedido(Material material,int size) throws NonexistentEntityException, Exception {
        MaterialJpaController mjpa = new MaterialJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        Pedido p = material.getPedido();
        material.setPedido(null);
        String nombre = material.getNombre();
        try {
            mjpa.edit(material);
            if (size < 2) {
                pjpa.destroy(p.getId());
            }
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,"Eliminado","Se elimino el material " + nombre + " del pedido."
        ));
        FacesContext.getCurrentInstance().getExternalContext().redirect("procesar.xhtml?pedido_id=" + pedido.getId());
    }
    
    public void despachar() throws Exception {
        MaterialJpaController mjpa = new MaterialJpaController(UtilidadDePersistencia.getEntityManagerFactory());
        try {
            for (Material material : pedido.getMateriales()) {
                material.setEstado("En préstamo");
                mjpa.edit(material);
            }
            pedido.setEstado("Activo");
            pjpa.edit(pedido);
            FacesContext.getCurrentInstance().getExternalContext().redirect("lista.xhtml");
        } catch (Exception ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,"Pedido en progreso","Se ha despachado el pedido y se le ha asignado la fecha de entrega " + pedido.getFechaEntrega()
        ));
    }
    
    public void guardarYBloquear() throws Exception {
        pedido.setEstado("Vencido");
        UsuarioJpa ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
        Usuario usuario = pedido.getUsuario();
        usuario.setActivo(false);
        try {
            ujpa.edit(usuario);
            pjpa.edit(pedido);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void desbloquearUsuario() {
        UsuarioJpa ujpa = new UsuarioJpa(UtilidadDePersistencia.getEntityManagerFactory());
        Usuario usuario = pedido.getUsuario();
        usuario.setActivo(true);
         try {
            ujpa.edit(usuario);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PedidoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void filtrar() throws IOException {
        ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
        String queryString = "?maxresultados=" + this.maxResultados;
        if (correo != null) {
            queryString += "&correo=" + correo;
        }
        if (estado != null) {
            queryString += "&estado=" + estado;
        }
        if(orden != null) {
            queryString += "&orden=" + orden;
        }
        if(modo != null) {
            queryString += "&modo=" + modo;
        }
        external.redirect("lista.xhtml" + queryString);
    }
    
    public List<Pedido> expirarPedidos() throws Exception {
        List<Pedido> ps = pjpa.pedidosPendientes();
        for (Pedido p : ps) {
            Date fecha = p.getFechaPedido();
            Calendar c = Calendar.getInstance();
            c.setTime(fecha);
            c.add(Calendar.DAY_OF_YEAR, 3);
            Date expira = c.getTime();
            if (hoy.after(expira)) {
                cancelar(p,false);
            }
        }
        return ps;
    }
    
    public List<Pedido> vencerPedidos() throws Exception {
        List<Pedido> ps = pjpa.pedidosActivos();
        for (Pedido p : ps) {
            if (hoy.after(p.getFechaEntrega())) {
                p.setEstado("Vencido");
                pjpa.edit(p);
                
            }
        }
        return ps;
    }
}
