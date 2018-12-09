/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.material.controlador;

import com.biologger.modelo.Categoria;
import com.biologger.modelo.UtilidadDePersistencia;
import com.biologger.modelo.jpa.CategoriaJpaController;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManagerFactory;


/**
 *
 * @author ikerlb
 */
@FacesConverter("catConverter")
public class CategoriaConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String newValue) {
        EntityManagerFactory emf = UtilidadDePersistencia.getEntityManagerFactory();
        CategoriaJpaController categoriaJPA = new CategoriaJpaController(emf);
        return categoriaJPA.findCategoria(Integer.parseInt(newValue));
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if (object == null) {
            return "";
        }
        if (object instanceof Categoria) {
            Categoria car = (Categoria) object;
            String name = car.getId().toString();
            return name;
        } else {
            throw new ConverterException(new FacesMessage(object + " no es una categoria valida"));
        }
    }
    
    
}   
