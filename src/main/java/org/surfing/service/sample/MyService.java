/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.surfing.Device;
import org.surfing.Thing;
import org.surfing.kernel.Kernel;
import org.surfing.service.audio.AudioTTS;
import org.surfing.service.mqtt.MQTTController;

/**
 *
 * @author vsenger
 */

public class MyService extends MQTTController {
    public void start() {
        super.start();
    }

    public void processMessage(String msg) {
        System.out.println("MyService processing this message: " + msg);
        for (Device device : Kernel.getInstance().getDevices()) {
            Thing t = device.getThings().get(msg);
            if(t.getName().equals(msg)) {
                AudioTTS.speak(msg + " value is " + t.getLastValue(), true);
            }
        }
    }
}
