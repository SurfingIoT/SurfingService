/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.rest;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.surfing.Service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vsenger
 */
public class RESTService extends Service {

    public static int PORT_NUMBER = 8090;

    HttpServer server;

    @Override
    public void start() {
        if (getConfig().getProperty("port.number") != null) {
            PORT_NUMBER = Integer.parseInt(getConfig().getProperty("port.number"));
        }

        try {
            DefaultResourceConfig resourceConfig = new DefaultResourceConfig(BasicService.class);
            //PackagesResourceConfig rc = new PackagesResourceConfig("org.surfing");
            server = GrizzlyServerFactory.createHttpServer("http://0.0.0.0:" + PORT_NUMBER);
            //server.setHeader("Access-Control-Allow-Origin", "*");
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, "Can't start Jersey.", ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        server.stop();

    }

    @Override
    public void run() {
    }

}
