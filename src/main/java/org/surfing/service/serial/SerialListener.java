/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.serial;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.surfing.Device;
import org.surfing.kernel.Kernel;
import org.surfing.service.mqtt.MQTTController;

/**
 *
 * @author vsenger
 */
@Path("/serial")
public class SerialListener extends MQTTController {

    @GET
    @Produces("text/html")
    @Path("/sensor/{device}/{sensor}")
    public String execute(@PathParam("device") String deviceName, @PathParam("sensor") String sensor) {
        if (deviceName != null && sensor != null) {
            for (Device d : Kernel.getInstance().devices) {
                if (d.getName().equals(deviceName)) {
                    try {
                        return d.getThings().get(sensor).execute(sensor);
                        
                    } catch (Exception ex) {
                        Logger.getLogger(SerialListener.class.getName()).log(Level.SEVERE, "Error sending Serial Message to {0}. {1}", new Object[]{d.getName(), ex.getMessage()});
                    }
                }
            }
        }
        return "not found";
    }

    @GET
    @Produces("text/html")
    @Path("/execute/{device}/{command}/{value}")
    public String execute(@PathParam("device") String device, @PathParam("command") String command, @PathParam("value") String value) {
        processMessage(device + "/" + command + "?" + value);
        return "Command " + command + " executed";
    }
    /*@GET
     @Produces("/execute/{command}")
     public String getMessage(@PathParam("command") String command) {
     processMessage(command);
     return "Command " + command + " executed.";
     }*/

    public void processMessage(String msg) {
        if (msg.length() > 0 && msg.contains("/")) {
            String deviceName = msg.substring(0, msg.indexOf("/"));
            String cmd = msg.substring(msg.indexOf("/") + 1, msg.length());
            if (deviceName != null && cmd != null) {
                for (Device d : Kernel.getInstance().devices) {
                    if (deviceName.equals("*") || d.getName().equals(deviceName)) {
                        try {
                            d.send(cmd);
                        } catch (IOException ex) {
                            Logger.getLogger(SerialListener.class.getName()).log(Level.SEVERE, "Error sending Serial Message to {0}. {1}", new Object[]{d.getName(), ex.getMessage()});
                        }
                    }
                }
            }
        }
    }
}
