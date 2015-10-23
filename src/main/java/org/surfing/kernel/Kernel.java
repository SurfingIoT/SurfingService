/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.surfing.Device;
import org.surfing.Service;
import org.surfing.Thing;
import org.surfing.service.camera.Camera;

/**
 *
 * @author vsenger
 */
public class Kernel {

    @Inject
    public Camera camera;
    
    //para uso em Java SE
    public static String DEVICE_NAME = "Surfing IoT Gateway";
    public static String APP_NAME = "Surfing IoT Gateway";
    public static String SYSTEM_TYPE = "pc"; //pc //single-board //microcontroller
    public static boolean AUDIO_ENABLE = false;

    public static long SERIALDISCOVERY_INTERVAL = 5000;

    public static final long startTimeStamp = System.currentTimeMillis();
    private static final long MIN_INTERVAL = 50;
    public Collection<Service> services;
    public Collection<Device> devices;
    public Map<String, Device> devicesTable;
    private long lastSend;
    public int mode = 1;

    private static Weld weld;
    private static WeldContainer container;

    public Kernel() {
        devices = new ArrayList<Device>();
        services = new ArrayList<Service>();
        devicesTable = new HashMap<String, Device>();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopServices();
                weld.shutdown();
            }
        });
    }

    public Camera getCamera() {
        return camera;
    }

    public void changeMode() {
        //ainda vou fazer com classes dinamicas
        if (++mode > 5) {
            mode = 1;
        }
    }

    public static void startWeld() {
        weld = new Weld();
        container = weld.initialize();

    }
    public static Kernel instance;

    public static Kernel getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        //fazer processo de restart automático (serviço?)
        //fazer processo de update automático (serviço?)
        startWeld();
        //Kernel k = new Kernel();
        //k.startWeld();
        //k.start();
        //System.out.println("Kernel Camera " + k.camera);
        instance = container.instance().select(Kernel.class).get();
        instance.start();
        try {
            for (;;) {
                Thread.sleep(60000);
            }
        } catch (Exception ex) {
            Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stopServices() {
        Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Stoping Surfing IoT Services");

        for (Service s : services) {
            Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Stoping " + s.getName());
            s.stop();
        }
    }

    public void weldContainer(@Observes ContainerInitialized event) {
        System.out.println("Event " + event);
    }

    public void start() {
        Properties prop = new Properties();

        //default /etc/surfing
        String path = System.getProperty("surfing.config", "/etc/surfing");
        try {
            prop.load(new FileInputStream(path + "/surfing.conf"));
        } catch (IOException ex) {
            Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (prop.getProperty("app.name") != null) {
            APP_NAME = prop.getProperty("app.name");
        }
        if (prop.getProperty("system.type") != null) {
            SYSTEM_TYPE = prop.getProperty("system.type");
        }
        if (prop.getProperty("audio.enable") != null) {
            AUDIO_ENABLE = Boolean.parseBoolean(prop.getProperty("audio.enable"));
        }
        String servicesPath = System.getProperty("surfing.service.path", path + "/services");
        File servicesFile = new File(servicesPath);
        if (servicesFile.exists()) {
            File servicesFiles[] = servicesFile.listFiles();
            initServices(servicesFiles);
        }

    }

    private void initServices(File[] servicesFiles) {
        Properties prop = null;
        for (File fileService : servicesFiles) {
            try {
                prop = new Properties();
                prop.load(new FileInputStream(fileService));
                //checking minimum parameters: name, class, interval, enabled
                if (prop.getProperty("name") == null || prop.getProperty("class") == null
                        || prop.getProperty("interval") == null || prop.getProperty("enabled") == null) {
                    Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, fileService.getName()
                            + " has invalid configuration for SurfThing Services, ignored.");
                } else {
                    if (Boolean.parseBoolean(prop.getProperty("enabled"))) {
                        String serviceName = prop.getProperty("name");
                        String serviceClass = prop.getProperty("class");
                        long interval = Long.parseLong(prop.getProperty("interval"));
                        Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Starting Service " + serviceName);
                        Class cs = Class.forName(serviceClass);
                        Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Service Class " + serviceClass + " loaded.");
                        Service service = (Service) cs.newInstance();
                        this.services.add(service);
                        service.setClassName(serviceClass);
                        service.setName(serviceName);
                        service.setInterval(interval);
                        service.setConfig(prop);
                        service.start();
                        if (interval > 0) {
                            Timer timer = new Timer(false);
                            timer.scheduleAtFixedRate(service, 0, interval);
                        }
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, "IO Error starting service " + fileService, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, "Class loading error " + fileService, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, "Instantiation error " + fileService, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, "Illegal Access  " + fileService, ex);
            }

        }
    }

    public static void delay(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized String getThingsString() {
        int t = 0;
        for (Device device : devices) {
            t += device.getThings().size();
        }
        String retornao = "things-server|" + t + "|";
        for (Device device : devices) {

            for (String c : device.getThings().keySet()) {
                Thing component = device.getThings().get(c);
                retornao += component.getName() + "|"
                        + component.getType() + "|"
                        + component.getPort() + "|"
                        + component.getLastValue() + "|";
            }
        }
        return retornao;
    }

    public synchronized Thing find(String id) {
        Thing thing = null;
        for (Device device : devices) {
            if (device.getThings().containsKey(id)) {
                thing = device.getThings().get(id);
            }
        }
        return thing;
    }

    public synchronized void timeControl() {
        if (System.currentTimeMillis() - lastSend < MIN_INTERVAL) {
            delay(System.currentTimeMillis() - lastSend);
        }
        lastSend = System.currentTimeMillis();
    }

    public synchronized void close() {
        if (devices == null || devices.isEmpty()) {
            return;
        }
        for (Device device : devices) {
            try {
                Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Closing device connection " + device.getName());
                devicesTable.remove(device.getName());
                devices.remove(device);
                device.close();
            } catch (IOException e) {
                Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Exception while closing " + device.getName());
            }
        }
    }

    public synchronized void close(String deviceName) {
        if (devices == null || devices.isEmpty()) {
            return;
        }
        Device device = devicesTable.get(deviceName);
        if (device == null) {
            return;
        }
        try {
            Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Closing device connection " + device.getName());
            devicesTable.remove(deviceName);
            devices.remove(device);
            device.close();

        } catch (IOException e) {
            Logger.getLogger(Kernel.class.getName()).log(Level.INFO, "Exception while closing " + device.getName());
        }
    }

    public synchronized void addDevice(Device device, String name) {
        if (devices == null) {
            devices = new ArrayList<Device>();
            devicesTable = new HashMap<String, Device>();
        }
        this.devicesTable.put(name, device);
        devices.add(device);
    }

    public synchronized Collection<Device> getDevices() {
        return devices;
    }

}
