/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.serial;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.surfing.Device;
import org.surfing.kernel.Kernel;
import org.surfing.service.mqtt.MQTTBaseService;
import org.surfing.service.rest.SensorManager;

/**
 *
 * @author vsenger
 */
public class SerialSensors extends MQTTBaseService {

    private String requestString = "sensors";

    @Override
    public void run() {
        Collection<Device> devices = Kernel.getInstance().getDevices();
        if (devices == null) {
            return;
        }
        for (Device device : devices) {
            try {
                //Logger.getLogger(SensorPublisher.class.getName()).log(Level.INFO,
                //       "Reading sensors from " + device.getName() + ", sending " + requestString);
                device.send("sensors");
                Kernel.delay(740);
                String s = device.receive();
                if (s != null && !s.equals("")) {
                    //Logger.getLogger(SensorPublisher.class.getName()).log(Level.INFO,
                    //        "Sending MQT Message " + s + " to QUEUE " + MQTTBaseService.MQTT_QUEUE + "/" + device.getName());
                    try {
                        SensorManager.newSensorData(device.getName(), s);
                    } catch (Exception e) {
                    }
                    sendMessage(s, MQTT_QUEUE + "/" + device.getName());
                }
            } catch (IOException ex) {
                Logger.getLogger(SerialSensors.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MqttException ex) {
                Logger.getLogger(SerialSensors.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
