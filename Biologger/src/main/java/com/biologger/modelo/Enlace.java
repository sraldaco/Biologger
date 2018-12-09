/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.modelo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alex aldaco
 */
@Entity
@Table(name = "enlace")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Enlace.findAll", query = "SELECT e FROM Enlace e")
    , @NamedQuery(name = "Enlace.findById", query = "SELECT e FROM Enlace e WHERE e.id = :id")
    , @NamedQuery(name = "Enlace.findByTitulo", query = "SELECT e FROM Enlace e WHERE e.titulo = :titulo")
    , @NamedQuery(name = "Enlace.findByUrl", query = "SELECT e FROM Enlace e WHERE e.url = :url")})
public class Enlace implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Size(max = 255)
    @Column(name = "titulo", length = 255)
    private String titulo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "url", nullable = false, length = 2147483647)
    private String url;
    @JoinColumn(name = "kit", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Kit kit;

    public Enlace() {
    }

    public Enlace(Integer id) {
        this.id = id;
    }

    public Enlace(Integer id, String url) {
        this.id = id;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
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
        if (!(object instanceof Enlace)) {
            return false;
        }
        Enlace other = (Enlace) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.biologger.modelo.Enlace[ id=" + id + " ]";
    }
    
}
