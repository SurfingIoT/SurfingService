package org.surfing.device;

import org.surfing.Device;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
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
import org.surfing.Thing;
import org.surfing.kernel.Kernel;

/**
 *
 * @author vsenger
 */
public class SerialDeviceRXTX implements Device, SerialPortEventListener {

    byte buffer[] = new byte[255];
    int bufferCounter = 0;

    public void addEventListener() {
    }
    private Timer timer;
    private static final int DEFAULT_BAUDRATE = 9600;
    final static int DISCOVERY_RETRY = 3;
    CommPortIdentifier portId;
    String portName;
    int baudRate;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;
    String resources;
    String name;
    String description;
    boolean connected;
    Map<String, Thing> things;
    Collection<Thing> thingsList;
    private Vector<String> toSend = new Vector<String>();

    public SerialDeviceRXTX(CommPortIdentifier portId, int baudRate) {
        this.portId = portId;
        this.baudRate = baudRate;
        things = new Hashtable<String, Thing>();
        thingsList = new ArrayList<Thing>();
    }

    public SerialDeviceRXTX(String portName, int baudRate) {
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
        Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                "Closing device on {0}", serialPort.getName());
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
                System.out.println("Closing FINAL " + serialPort.getName() + " port");
                serialPort.close();
                System.out.println(portId.getCurrentOwner());

            }
        } catch (Exception e) {
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public synchronized void open() throws IOException {
        try {
            if (portName != null) {
                portId
                        = CommPortIdentifier.getPortIdentifier(portName);
            }
            if (portId == null) {
                throw new IOException("Invalid port " + portName);
            }
            serialPort
                    = (SerialPort) portId.open(portName, baudRate);
            if (portId == null) {
                throw new IOException("Invalid port " + portName);
            }

            serialPort.setSerialPortParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.notifyOnOutputEmpty(true);

            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                    "Connection Stabilished with {0}", serialPort.getName());
            Kernel.delay(2000);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.SEVERE,
                    "Could not init the device on " + serialPort.getName(), e);
            serialPort.close();
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
                Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
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
                    Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                            "Wrong resource String. Parse error!", e);
                }
            } else {
                Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                        "Empty Resource String - Nor a Surfboard Device...", resources);
            }

            Kernel.delay(2500);

        }

    }

    public synchronized void send(char s) throws IOException {

        if (outputStream == null) {
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.SEVERE,
                    "This device ({0}) is not working because IO objects are null. "
                    + "You should restart the device!", this.getName());
        } else {
            outputStream.write(s);
            outputStream.flush();

        }
    }

    @Override
    public synchronized void send(String s) throws IOException {

        if (outputStream == null) {
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.SEVERE,
                    "This device ({0}) is not working because IO objects are null. "
                    + "You should restart the device!", this.getName());
        } else {
            outputStream.write(s.getBytes());
            outputStream.flush();

        }
    }

    @Override
    public synchronized String receive() throws IOException {

        if (inputStream == null) {
            String msg = "This device (" + this.getName()
                    + ") is not working because IO objects are null. "
                    + "You should restart the device!";
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.SEVERE, msg);
            throw new IOException(msg);
        } else {

            int available = inputStream.available();
            if (available == 0) {
                //inputStream.close();
                return null;
            } else {
                byte chunk[] = new byte[available];
                inputStream.read(chunk, 0, available);
                String retorno = new String(chunk);
                inputStream.close();
                return retorno;
            }
        }
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
        return this.portId.getName();
    }

    @Override
    public Collection<Thing> getThingsList() {
        return thingsList;
    }

    public Collection<Device> scanPorts() {

        Collection<Device> devicesFound = new ArrayList<Device>();

        Enumeration portList;
        CommPortIdentifier portId;

        portList = CommPortIdentifier.getPortIdentifiers();
        //Logger.getLogger(SerialDevice.class.getName()).log(Level.INFO, "Starting scan port");
        SerialPort serialPort = null;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                    "Scaning port: " + portId.getName());

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                        "Serial Device Port found " + portId.getName()
                        + ". Trying to discovery this device.");
                try {
                    /*serialPort =
                     (SerialPort) portId.open(portId.getName(), 115200);
                     Device device = new SerialDevice(serialPort);*/
                    Device device = new SerialDeviceRXTX(portId, DEFAULT_BAUDRATE);
                    device.open();
                    device.discovery();
                    if (device.connected()) {
                        devicesFound.add(device);
                    } else {
                        Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.INFO,
                                "Serial Device is not Things API Compatible" + portId.getName());
                        device.close();
                    }
                    devicesFound.add(device);
                } catch (Exception e) {
                    Logger.getLogger(SerialDeviceRXTX.class.getName()).log(Level.SEVERE,
                            "Couldn't connect to" + portId.getName());
                    e.printStackTrace();
                }
            }
        }
        return devicesFound;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = inputStream.available();
                while (inputStream.available() > 0) {
                    buffer[bufferCounter++] = (byte) inputStream.read();
                    if (buffer[bufferCounter - 1] == '\n') {
                        System.out.println(new String(buffer));
                        //tratar();
                    }
                    if (bufferCounter > 255) {
                        //resetBuffer();
                    }
                }

                // Displayed results are codepage dependent
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
            }
        }

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
