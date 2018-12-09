/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.biologger.servicio;

import org.jsoup.Jsoup;

/**
 *
 * @author alex aldaco
 */
public class HtmlToString {
    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }
}
