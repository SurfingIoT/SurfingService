/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.surfing.kernel.Kernel;

/**
 *
 * @author vsenger
 */
public abstract class MQTTController extends MQTTBaseService implements MqttCallback {

    Kernel kernel = Kernel.getInstance();

    @Override
    public void start() {
        super.start();
        fixConnection();
    }

    @Override
    public void fixConnection() {
        try {
            super.fixConnection();
            client.setCallback(this);
            Logger.getLogger(MQTTController.class.getName()).log(Level.INFO, "MQTT Receiver Subscribing {0}", MQTT_QUEUE_SUBSCRIBE);
            client.subscribe(MQTT_QUEUE_SUBSCRIBE);
        } catch (MqttException ex) {
            Logger.getLogger(MQTTController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        if (client == null || !client.isConnected()) {
            Logger.getLogger(MQTTController.class.getName()).log(Level.INFO, "MQTT Receiver Lost Connection, trying to recovery once");
            fixConnection();
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        Logger.getLogger(MQTTController.class.getName()).log(Level.INFO, "MQTT Receiver Lost Connection, trying to recovery once");
        fixConnection();
    }


    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        //surfboard2/relay1?1

        String msg = mm.toString();
        if (msg == null) {
            return;
        }

        processMessage(msg);
    }

    public abstract void processMessage(String msg);

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {

    }

}
