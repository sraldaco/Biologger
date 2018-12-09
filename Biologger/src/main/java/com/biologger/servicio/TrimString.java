/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.servicio;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author alex aldaco
 */
@ManagedBean(name="trimString")
@ViewScoped
public class TrimString {

    public String resumen(String html,int length) {
        String str = HtmlToString.html2text(html);
        if(str.length() > length)
            str = str.substring(0,length) + "...";
        return str;
    }
    
    public String stringify(String html){
        return HtmlToString.html2text(html);
    }
}
