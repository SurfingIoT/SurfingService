/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.surfing.Kernel;

/**
 *
 * @author vsenger
 */
public class WatchDog extends MQTTBaseService {

    public String WATCHDOG_QUEUE = MQTT_QUEUE + "/devices";

    @Override
    public void start() {
        super.start();
        if (getConfig().getProperty("watchdog.queue") != null) {
            WATCHDOG_QUEUE = getConfig().getProperty("watchdog.queue");
        }
    }

    @Override
    public void run() {
        long t = System.currentTimeMillis() - Kernel.startTimeStamp;
        long horas = (((t / 1000) / 60) / 60);
        long minutos;
        if (horas > 0) {
            minutos = (t - (horas * 60 * 60 * 1000)) / 1000 / 60;
        } else {
            minutos = t / 1000 / 60;
        }
        try {
            //Watch Dog Actions
            sendMessage(Kernel.APP_NAME + " on "
                    + horas + " hora(s) e " + minutos + " minuto(s)", MQTT_QUEUE + "/" + WATCHDOG_QUEUE);
        } catch (MqttException ex) {
            Logger.getLogger(MQTTBaseService.class.getName()).log(Level.SEVERE, "Failed to send watchdog message", ex);
        }
        /*try {
         Logger.getLogger(MQTTPublisherService.class.getName()).log(Level.INFO,
         "Reading sensors from " + device.getName() + ", sending " + sensors);

         device.send(sensors);
         Kernel.delay(740);
         String s = device.receive();
         if (s != null && !s.equals("")) {
         Logger.getLogger(MQTTPublisherService.class.getName()).log(Level.INFO,
         "Sending MQT Message " + s + " to QUEUE " + MQTTBaseService.MQTT_QUEUE + "/" + device.getName());

         sendMessage(s, MQTTBaseService.MQTT_QUEUE + "/" + device.getName());
         }
         } catch (IOException ex) {
         Logger.getLogger(MQTTPublisherService.class.getName()).log(Level.SEVERE, null, ex);
         } catch (MqttException ex) {
         Logger.getLogger(MQTTPublisherService.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }
}
