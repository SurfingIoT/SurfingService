/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.device;

import java.io.IOException;
import org.surfing.Device;
import org.surfing.kernel.Kernel;

/**
 *
 * @author vinic_000
 */
public class IoTSurfboard {

    Device board;

    public IoTSurfboard(String port, int baudRate) throws IOException {
        board = new SerialDeviceJSSC(port, baudRate);
        board.open();
        Kernel.delay(2500);
    }

    public IoTSurfboard(String port, int baudRate, String API) throws IOException {
        if (API.equals("RXTX")) {
            board = new SerialDeviceRXTX(port, baudRate);
        } else if (API.equals("JSSC")) {
            board = new SerialDeviceJSSC(port, baudRate);
        } else {
            System.out.println("API Name not recognized!");
        }
        board.open();
        Kernel.delay(2500);
    }

    public void close() throws IOException {
        board.close();
    }

    public int alcohol() throws IOException {
        board.send("alcohol");
        Kernel.delay(100);
        return Integer.parseInt(board.receive());
    }

    public int light() throws IOException {
        board.send("light");
        Kernel.delay(100);
        return Integer.parseInt(board.receive());
    }

    public int potentiometer() throws IOException {
        board.send("pot");
        Kernel.delay(100);
        return Integer.parseInt(board.receive());

    }

    public float temperature() throws IOException {
        board.send("temp");
        Kernel.delay(350);
        return Float.parseFloat(board.receive());

    }

    public float humidity() throws IOException {
        board.send("humidity");
        Kernel.delay(350);
        return Float.parseFloat(board.receive());
    }

    public void transistor(boolean v) throws IOException {
        board.send("transistor?" + (v ? "1" : "0"));
    }

    public String clock() throws IOException {
        board.send("clock");
        Kernel.delay(100);
        return board.receive();
    }

    public void relay(boolean v) throws IOException {
        board.send("relay?" + (v ? "1" : "0"));
    }

    public void speaker(boolean v) throws IOException {
        board.send("speaker?" + (v ? "1" : "0"));

    }

    public void red(int p) throws IOException {
        board.send("red?" + p);

    }

    public void green(int p) throws IOException {
        board.send("green?" + p);

    }

    public void blue(int p) throws IOException {
        board.send("blue?" + p);

    }

    public void rgb(int r, int g, int b) throws IOException {
        this.red(r);
        Kernel.delay(50);

        this.green(g);
        Kernel.delay(50);

        this.blue(b);
        Kernel.delay(50);

    }

    public void servo(int s) throws IOException {
        board.send("servo?" + s);

    }

    public int distance() throws IOException {
        board.send("distance");
        Kernel.delay(150);
        return Integer.parseInt(board.receive());

    }
}
