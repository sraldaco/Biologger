/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.validaciones;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;



/**
 *
 * @author alex aldaco
 */
@FacesValidator(value="UploadImageValidator")
public class ValidacionSubidaImagenes implements Validator{
 
    @Override
    public void validate(FacesContext context, UIComponent component, Object value)     
            throws ValidatorException {
        Part file = (Part) value;
        FacesMessage message = null;
        try {
            if (file.getSize()>8000000) {
                message = new FacesMessage("El peso máximo permitido para la foto es de 1MB.");
            }
 
            if (message!=null && !message.getDetail().isEmpty()) {
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(message );
            }
        } catch (ValidatorException ex) {
            throw new ValidatorException(new FacesMessage(ex.getMessage()));
        }
 
    }


}
