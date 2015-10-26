/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.serial;

import org.surfing.device.SerialDevice;
import gnu.io.CommPortIdentifier;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.surfing.Device;
import org.surfing.kernel.Kernel;
import org.surfing.Service;
import org.surfing.service.audio.AudioTTS;

/**
 *
 * @author vsenger
 */
public class DiscoveryService extends Service {

    Kernel kernel = Kernel.getInstance();
    public static String PORTS;
    public static String PORT_DISCOVERY_DIR = "";
    public static boolean PORT_SWEEPING;
    public static boolean PORT_SCAN;
    private static int PORT_SWEEPING_MAX = 5; //with this discovery will be looking for ttyUSB0 .. ttyUSB9
    public static int DEFAULT_BAUD_RATE = 9600;
    private boolean working;
    private ArrayList<String> serialPorts = new ArrayList<String>();
    Thread dirObserver;

    @Override
    public void start() {
        if (getConfig().getProperty("ports") != null) {
            PORTS = getConfig().getProperty("ports");
            try {
                StringTokenizer st = new StringTokenizer(PORTS, ":");
                while (st.hasMoreElements()) {
                    serialPorts.add((String) st.nextElement());
                }
            } catch (Exception e) {
            }
            //will create a JVM property that will help to find rare useless names like /dev/rfcomm..
            gnuRXTXInit();
        }
        if (getConfig().getProperty("port.discovery.dir") != null) {
            PORT_DISCOVERY_DIR = getConfig().getProperty("port.discovery.dir");
        }
        if (getConfig().getProperty("port.sweeping") != null) {
            PORT_SWEEPING = Boolean.parseBoolean(getConfig().getProperty("port.sweeping"));
        }
        if (getConfig().getProperty("port.sweeping.max") != null) {
            PORT_SWEEPING_MAX = Integer.parseInt(getConfig().getProperty("port.sweeping.max"));
        }
        if (getConfig().getProperty("port.scan") != null) {
            PORT_SCAN = Boolean.parseBoolean(getConfig().getProperty("port.scan"));
        }
        if (getConfig().getProperty("baud.rate") != null) {
            DEFAULT_BAUD_RATE = Integer.parseInt(getConfig().getProperty("baud.rate"));
        }
    }

    @Override
    public void stop() {
    }

    private void gnuRXTXInit() {
        String arg = "";
        for (String port : serialPorts) {
            for (int x = 0; x < PORT_SWEEPING_MAX; x++) {
                arg += port + x + ":";
            }
        }
        System.setProperty("gnu.io.rxtx.SerialPorts", arg);
        System.setProperty("gnu.io.SerialPorts", arg);
    }

    @Override
    public synchronized void run() {
        if (!PORT_DISCOVERY_DIR.equals("")) {
            //System.out.println("starting file observer");
            if (dirObserver == null || dirObserver != null && dirObserver.isInterrupted()) {
                dirObserver = new DirObserver(PORT_DISCOVERY_DIR);
                dirObserver.start();
            }
            if (working) {
                return;
            }
        }
        if (PORT_SWEEPING) {
            for (String port : serialPorts) {
                for (int x = 0; x < PORT_SWEEPING_MAX; x++) {
                    File f = new File(port + x);
                    if (f.exists() && !kernel.devicesTable.containsKey(f.getAbsolutePath())) {
                        addSerialDevice(f.getAbsolutePath());
                    }
                }
            }
        }
        if (PORT_SCAN) {
            scanPorts();
        }
    }

    public Collection<Device> scanPorts() {

        Collection<Device> devicesFound = new ArrayList<Device>();

        Enumeration portList;
        CommPortIdentifier portId;
        Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Starting scan port");

        portList = CommPortIdentifier.getPortIdentifiers();
        Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Ports to scan: " + (portList.hasMoreElements() ? "found!" : "not found!"));
        while (portList.hasMoreElements()) {
            boolean isListed = false;
            portId = (CommPortIdentifier) portList.nextElement();
            for (Device dd : Kernel.getInstance().getDevices()) {
                isListed = dd.getPortName().equals(portId.getName());
            }
            if (!isListed) {
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Scaning port: {0}", portId.getName());

                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Serial Device Port found {0}. Trying to discovery this device.", portId.getName());
                    try {
                        addSerialDevice(portId.getName());
                    } catch (Exception e) {
                        Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, "Couldn''t connect to{0}", portId.getName());
                    }
                }
            }
        }
        return devicesFound;
    }

    public synchronized Device discoverySerial(String serial, int baudRate) throws Exception {
        SerialDevice device
                = new SerialDevice(serial, baudRate);
        device.open();
        device.discovery();
        if (device.getResourceString() == null) {
            device.close();
            return null;
        } else {
            return device;
        }
    }

    public synchronized void addSerialDevice(String name) {
        Device discovered = null;
        try {
            discovered = discoverySerial(name, DEFAULT_BAUD_RATE);
            if (discovered == null) {
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Device {0} has no discovery reponse!", name);
                return;
            }
        } catch (Exception ex) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, "Error add / discovering Serial Device {0}. {1}", new Object[]{name, ex.getMessage()});
            System.out.println("Closing " + name);
            kernel.close(name);
            return;
        }
        Kernel.getInstance().addDevice(discovered, name);
        Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "New device discovered {0}", discovered.getName());
        AudioTTS.speak(discovered.getName() + " pluged into " + Kernel.APP_NAME, true);
    }

    class DirObserver extends Thread {

        String dir;

        public DirObserver(String dir) {
            this.dir = dir;
        }

        @Override
        public synchronized void run() {
            try {
                Path deviceFolder = Paths.get(dir);
                WatchService watchService = FileSystems.getDefault().newWatchService();
                deviceFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);

                boolean valid = true;
                do {
                    WatchKey watchKey = watchService.take();

                    for (WatchEvent event : watchKey.pollEvents()) {
                        WatchEvent.Kind kind = event.kind();
                        if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            if (isSomethingValid(fileName)) {
                                working = true;
                                System.out.println("File Created:" + dir + "/" + fileName + " waiting 2000ms...");
                                kernel.delay(2000);
                                addSerialDevice(dir + "/" + fileName);
                                working = false;

                            }
                        }
                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                            String fileName = event.context().toString();
                            if (!fileName.equals("fuse")) {
                                if (isSomethingValid(fileName)) {
                                    //System.out.println("File Modified:" + dir + "/" + fileName);
                                }
                            }
                        }
                        if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                            String fileName = event.context().toString();
                            if (isSomethingValid(fileName)) {
                                System.out.println("File Deleted:" + dir + "/" + fileName);
                            }
                        }
                    }
                    valid = watchKey.reset();

                } while (valid);
            } catch (Exception e) {
            }

        }

    }

    public static boolean isSomethingValid(String name) {
        return name.contains(".conf") || name.contains("USB") || name.contains("rfcomm") || name.contains("ACM") || name.contains("AMA") || name.contains("COM");
    }

}
