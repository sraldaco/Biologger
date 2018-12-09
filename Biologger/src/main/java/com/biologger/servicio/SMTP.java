/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.biologger.servicio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author alex aldaco
 */
public class SMTP {
    
    public static void enviarCorreo(String destinatario, String asunto, String mensaje)
            throws MessagingException, IOException {
        try {
            Properties props = System.getProperties();
            Properties c = new Properties();
            InputStream inputStream = SMTP.class.getClassLoader().getResourceAsStream("correo.properties");
            c.load(inputStream);
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", c.getProperty("puerto")); 
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            Session sesion = Session.getDefaultInstance(props);

            MimeMessage correo = new MimeMessage(sesion);
            correo.setFrom(new InternetAddress(c.getProperty("correo"),c.getProperty("remitente")));
            correo.setRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            correo.setSubject(asunto);
            correo.setContent(mensaje,"text/html");
        
            Transport transport = sesion.getTransport();
            transport.connect(c.getProperty("host"), c.getProperty("usuario"), c.getProperty("contrasena"));
            transport.sendMessage(correo, correo.getAllRecipients());
        
        } catch (MessagingException ex) {
            throw ex;
        }  
    }
}
