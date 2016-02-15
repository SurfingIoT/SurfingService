package org.surfing.device;

import org.surfing.Device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.surfing.Thing;
import org.surfing.kernel.Kernel;

/**
 *
 * @author vsenger
 */
public class SerialDeviceJSSC implements Device {

    public static void main(String[] args) throws Exception {
        SerialDeviceJSSC test = new SerialDeviceJSSC("COM4", 9600);
        test.open();
        test.send("discovery");
        Kernel.delay(2000);
        System.out.println(test.receive());
    }
    byte buffer[] = new byte[255];
    int bufferCounter = 0;

    public void addEventListener() {
    }
    private Timer timer;
    private static final int DEFAULT_BAUDRATE = 9600;
    final static int DISCOVERY_RETRY = 3;
    String portName;
    int baudRate;
    private SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;
    String resources;
    String name;
    String description;
    boolean connected;
    Map<String, Thing> things;
    Collection<Thing> thingsList;
    private Vector<String> toSend = new Vector<String>();

    public SerialDeviceJSSC(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
        things = new Hashtable<String, Thing>();
        thingsList = new ArrayList<Thing>();
    }

    @Override
    public boolean connected() {
        return connected;
    }

    @Override
    public synchronized void close() throws IOException {
        Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                "Closing device on {0}", this.getPortName());
        //send("X");
        connected = false;
        try {
            inputStream.close();
        } catch (Exception e) {
        }
        try {
            outputStream.close();
        } catch (Exception e) {
        }
        try {
            if (serialPort != null) {
                System.out.println("Closing FINAL " + serialPort.getPortName()
                        + " port");
                serialPort.closePort();
            }
        } catch (Exception e) {
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public synchronized void open() throws IOException {
        try {
            serialPort = new SerialPort(portName);

            if (serialPort.openPort()) {
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
            }
            
            Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                    "Connection Stabilished with {0}", serialPort.getPortName());
            Kernel.delay(2000);
        } catch (Exception e) {
            try {
                Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE,
                        "Could not init the device on " + serialPort.getPortName(), e);
                serialPort.closePort();
            } catch (SerialPortException ex) {
                Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public synchronized void discovery() throws Exception {
        //Those times are totally dependent with the kind of communication...
        System.out.println("initial data #1 " + receive());
        Kernel.delay(500);
        System.out.println("initial data #2 " + receive());
        Kernel.delay(500);
        System.out.println("initial data #3 " + receive());
        Kernel.delay(500);
        for (int x = 0; x < DISCOVERY_RETRY; x++) {
            System.out.println("Discovery - try no." + (x + 1));
            send("discovery");
            //Kernel.delay(10);

            resources = receive();

            if (resources != null) {
                Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                        "Compatible Device found! Resource String: {0}", resources);
                things = new Hashtable<String, Thing>();
                thingsList = new ArrayList<Thing>();

                connected = true;
                try {
                    StringTokenizer tokenizer = new StringTokenizer(resources, "|");
                    this.name = tokenizer.nextToken();
                    int numberOfComponents = Integer.parseInt(tokenizer.nextToken());
                    for (int y = 0; y < numberOfComponents; y++) {
                        String name = tokenizer.nextToken();
                        String type = tokenizer.nextToken();
                        String port = tokenizer.nextToken();
                        String value = tokenizer.nextToken();

                        Thing component = new Thing(this, name, name, port, type, value);
                        this.things.put(name, component);
                        this.thingsList.add(component);

                    }

                    break;
                } catch (Exception e) {
                    Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                            "Wrong resource String. Parse error!", e);
                }
            } else {
                Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                        "Empty Resource String - Nor a Surfboard Device...", resources);
            }

            Kernel.delay(2500);

        }

    }

    public synchronized void send(char s) throws IOException {
        if (serialPort == null) {
            Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE,
                    "This device ({0}) is not working because IO objects are null. "
                    + "You should restart the device!", this.getName());
        } else {
            try {
                serialPort.writeString("" + s);
            } catch (SerialPortException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public synchronized void send(String s) throws IOException {
        if (serialPort == null) {
            Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE,
                    "This device ({0}) is not working because IO objects are null. "
                    + "You should restart the device!", this.getName());
        } else {
            try {
                serialPort.writeString(s);
            } catch (SerialPortException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public synchronized String receive() throws IOException {
        String r = null;
        if (serialPort == null) {
            Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE,
                    "This device ({0}) is not working because IO objects are null. "
                    + "You should restart the device!", this.getName());
        } else {
            try {
                r = serialPort.readString();
            } catch (SerialPortException ex) {
                throw new IOException(ex);
            }
        }
        return r;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getResourceString() {
        return resources;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Map<String, Thing> getThings() {
        return this.things;
    }

    @Override
    public String getID() {
        return this.serialPort.getPortName();
    }

    @Override
    public Collection<Thing> getThingsList() {
        return thingsList;
    }

    public Collection<Device> scanPorts() {

        /*Collection<Device> devicesFound = new ArrayList<Device>();

        Enumeration portList;
        CommPortIdentifier portId;

        portList = CommPortIdentifier.getPortIdentifiers();
        //Logger.getLogger(SerialDevice.class.getName()).log(Level.INFO, "Starting scan port");
        SerialPort serialPort = null;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                    "Scaning port: " + portId.getName());

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                        "Serial Device Port found " + portId.getName()
                        + ". Trying to discovery this device.");
                try {
                    Device device = new SerialDeviceJSSC(portId, DEFAULT_BAUDRATE);
                    device.open();
                    device.discovery();
                    if (device.connected()) {
                        devicesFound.add(device);
                    } else {
                        Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.INFO,
                                "Serial Device is not Things API Compatible" + portId.getName());
                        device.close();
                    }
                    devicesFound.add(device);
                } catch (Exception e) {
                    Logger.getLogger(SerialDeviceJSSC.class.getName()).log(Level.SEVERE,
                            "Couldn't connect to" + portId.getName());
                    e.printStackTrace();
                }
            }
        }*/
        //return devicesFound;
        return null;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    @Override
    public void setTimerControl(Timer t) {
        this.timer = t;
    }

    @Override
    public Timer getTimerControl() {
        return timer;
    }

    public List<String> getSendQueue() {
        return toSend;
    }

}
