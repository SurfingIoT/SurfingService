/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.things;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.surfing.Device;
import org.surfing.Thing;
import org.surfing.kernel.Kernel;
import org.surfing.service.mqtt.MQTTController;
import org.surfing.service.persistence.Persistence;

/**
 *
 * @author vsenger
 */
@Path("/things")
public class Manager extends MQTTController {

    private final String requestString = "sensors";

    @Override
    public synchronized void run() {
        Collection<Device> devices = Kernel.getInstance().getDevices();
        if (devices == null) {
            return;
        }
        for (Device device : devices) {
            try {
                Logger.getLogger(Manager.class.getName()).log(Level.INFO,
                       "Reading sensors from " + device.getName() + ", sending " + requestString);
                device.send("sensors");
                Kernel.delay(940);
                String s = device.receive();
                if (s != null && !s.equals("")) {
                    //Logger.getLogger(SensorPublisher.class.getName()).log(Level.INFO,
                    //        "Sending MQT Message " + s + " to QUEUE " + MQTTBaseService.MQTT_QUEUE + "/" + device.getName());
                    try {
                        newSensorData(device.getName(), s);
                    } catch (Exception e) {
                    }
                    sendMessage(s, MQTT_QUEUE + "/" + device.getName());
                    Persistence.save(s, "sensors");
                }
            } catch (IOException ex) {
                try {
                    device.close();
                } catch (IOException ex1) {
                    Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex1);
                }
                Kernel.getInstance().devices.remove(device);
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MqttException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    



    public static void newSensorData(String deviceName, String data) {
        Device found = null;
        for (Device device : Kernel.getInstance().getDevices()) {
            if (device.getName().equals(deviceName)) {
                found = device;
            }
        }
        if (found == null) {
            return;
        }
        JSONObject jsonObject = (JSONObject) JSONValue.parse(data);
        if (jsonObject == null || jsonObject.keySet() == null || jsonObject.keySet().iterator() == null) {
            //System.out.println("Erro json " + data);
            return;
        }

        JSONArray components = (JSONArray) jsonObject.get("components");

        Iterator i = components.iterator();
        while (i.hasNext()) {
            Object oo = i.next();
            JSONObject joo = (JSONObject) oo;
            String thing = joo.get("name").toString();
            String value = joo.get("value").toString();
            found.getThings().get(thing).setLastValue(value);

        }

    }
    @GET
    @Produces("text/html")
    @Path("/data/{device}/{sensor}")
    public String execute(@PathParam("device") String deviceName, @PathParam("sensor") String sensor) {
        for (Device device : Kernel.getInstance().getDevices()) {
            if (device.getName().equals(deviceName)) {
                Thing thing = device.getThings().get(sensor);
                return thing.getLastValue();
            } else {
                return "Sensor not found";
            }
        }
        return "Device not found";
    }
    @GET
    @Produces("text/html")
    @Path("/{device}/{sensor}")
    public String read(@PathParam("device") String deviceName, @PathParam("sensor") String sensor) {
        if (deviceName != null && sensor != null) {
            for (Device d : Kernel.getInstance().devices) {
                if (d.getName().equals(deviceName)) {
                    try {
                        return d.getThings().get(sensor).execute(sensor);
                        
                    } catch (Exception ex) {
                        Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Error sending Serial Message to {0}. {1}", new Object[]{d.getName(), ex.getMessage()});
                    }
                }
            }
        }
        return "not found";
    }

    @GET
    @Produces("text/html")
    @Path("/{device}/{command}/{value}")
    public String execute(@PathParam("device") String device, @PathParam("command") String command, @PathParam("value") String value) {
        processMessage(device + "/" + command + "?" + value);
        return "Command " + command + " executed";
    }

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
                            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, "Error sending Serial Message to {0}. {1}", new Object[]{d.getName(), ex.getMessage()});
                        }
                    }
                }
            }
        }
    }
}
