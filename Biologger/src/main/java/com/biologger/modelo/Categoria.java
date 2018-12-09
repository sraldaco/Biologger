/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author alex aldaco
 */
@Entity
@Table(name = "categoria", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Categoria.findAll", query = "SELECT c FROM Categoria c")
    , @NamedQuery(name = "Categoria.findById", query = "SELECT c FROM Categoria c WHERE c.id = :id")
    , @NamedQuery(name = "Categoria.findByNombre", query = "SELECT c FROM Categoria c WHERE c.nombre = :nombre")})
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
    @OneToMany(mappedBy = "padre")
    private List<Categoria> hijas;
    @JoinColumn(name = "padre", referencedColumnName = "id")
    @ManyToOne
    private Categoria padre;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "categoria")
    private List<Rmc> rmcList;

    public Categoria() {
    }

    public Categoria(Integer id) {
        this.id = id;
    }

    public Categoria(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @XmlTransient
    public List<Categoria> getHijas() {
        return hijas;
    }

    public void setHijas(List<Categoria> hijas) {
        this.hijas = hijas;
    }

    public Categoria getPadre() {
        return padre;
    }

    public void setPadre(Categoria padre) {
        this.padre = padre;
    }
    
    /* Devuelve una lista con todas las relaciones que existen entre materiales y categorias
        * Para acceder a los materiales desde esta lista debes iterar sobre la lista y usar el
        * método definido en RMC.class getMaterial(), ejemplo:
        * categoría1 es la categoría con la que estas trabajando, entonces para obtener los materiales 
        * relacionados a cetegoria1 debes sacar la lista de las relaciones y guardarlas en una lista de Rmc
        * List<Rmc> relaciones = categoria1.getRmcList();
        * List<Materiales> materiales = new ArrayList();
        * for(Rmc relacion : relaciones {
        *    materiales.add(relacion.getMaterial());
        * } 
        */
    @XmlTransient
    public List<Rmc> getRmcList() {
        return rmcList;
    }

    public void setRmcList(List<Rmc> rmcList) {
        this.rmcList = rmcList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Categoria)) {
            return false;
        }
        Categoria other = (Categoria) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.biologger.modelo.Categoria[ id=" + id + " ]";
    }
    
}
