/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.rest;

import java.util.Iterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.surfing.Device;
import org.surfing.kernel.Kernel;
import org.surfing.Service;
import org.surfing.Thing;

/**
 *
 * @author vsenger
 */
@Path("/sensor")
public class SensorManager extends Service {

    @GET
    @Produces("text/html")
    @Path("/read/{device}/{sensor}")
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

    public static void main(String[] args) {
        newSensorData("sss", "{\"alcohol\": 23, \"temp\" : 24.4}");
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

        Iterator i = jsonObject.keySet().iterator();
        //System.out.println("atualizando objetos");
        while (i.hasNext()) {
            String thing = (String) i.next();
            Object value = jsonObject.get(thing);
            //System.out.println("Thing" + thing + " Value " + value);
            found.getThings().get(thing).setLastValue(value.toString());
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void run() {
    }

}
