/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing;

import java.util.Properties;
import java.util.TimerTask;

/**
 *
 * @author vsenger
 */
public abstract class Service extends TimerTask {

    Properties config;
    long interval;

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public abstract void start();

    public abstract void stop();


    public void restart() {
        stop();
        start();
    }

    public long interval() {
        return interval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    String name;
    String className;

    public void setInterval(long interval) {
        this.interval=interval; 
    }

}
