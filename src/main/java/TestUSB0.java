/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.surfing.Device;
import org.surfing.device.IoTSurfboard;
import org.surfing.kernel.Kernel;
import static org.surfing.kernel.Kernel.*;
import org.surfing.device.SerialDeviceRXTX;

/**
 *
 * @author vsenger
 */
public class TestUSB0 {

    public static void main(String[] args) {
        IoTSurfboard board = null;
        try {
            //by default it will use JSSC
            board = new IoTSurfboard("COM4", 9600);
            //to use RXTX
            //board = new IoTSurfboard("COM5", 9600, "RXTX");
            while (true) {
                System.out.println("Alcohol      :" + board.alcohol());
                System.out.println("Temperature  :" + board.temperature());
                System.out.println("Humidity     :" + board.humidity());
                System.out.println("Light        :" + board.light());
                System.out.println("Potentiometer:" + board.potentiometer());
                System.out.println("Clock        :" + board.clock());
                System.out.println("Red Light");
                board.red(255);
                Kernel.delay(500);
                board.red(0);
                Kernel.delay(500);
                System.out.println("Green Light");
                board.green(255);
                Kernel.delay(500);
                board.green(0);
                Kernel.delay(500);
                System.out.println("Blue Light");
                board.blue(255);
                Kernel.delay(500);
                board.blue(0);
                Kernel.delay(500);
                System.out.println("Transistor T1");
                board.transistor(true);
                Kernel.delay(500);
                board.transistor(false);
                Kernel.delay(500);
                System.out.println("Relay");
                board.relay(true);
                Kernel.delay(500);
                board.relay(false);
                Kernel.delay(500);
                System.out.println("Speaker");
                board.speaker(true);
                Kernel.delay(200);
                board.speaker(false);
                Kernel.delay(200);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                board.close();
            } catch (IOException ex) {
                Logger.getLogger(TestUSB0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*Device things = new SerialDevice("COM3", 9600);
        things.open();
        Kernel.delay(2500);
        things.send("sensors");
        Kernel.delay(740);
        String s = things.receive();
        System.out.println("Sensors: " + s);*/
        //things.send("/dev/ttyUSB0", "sl");
        //delay(3000);
        /*things.send("frente?5");
         delay(3000);
         things.send("parar");
         delay(1000);
         things.send("re?5");
         delay(3000);
         things.send("parar");

         delay(1000);*/

 /*String t;
         for (int x = 0; x < 100; x++) {
         things.send("light");
         delay(30);
         t = things.receive();
         System.out.println("light " + t);

         things.send("alcohol");
         delay(50);
         t = things.receive();
         System.out.println("alcohol " + t);

         things.send("pot");
         delay(50);
         t = things.receive();
         System.out.println("potenciometer " + t);
            
         things.send("distance");
         delay(350);
         t = things.receive();
         System.out.println("distance " + t);
            
         things.send("clock");
         delay(350);
         t = things.receive();
         System.out.println("date / time " + t);            
         things.send("temp");
         delay(350);
         t = things.receive();
         System.out.println("temperature " + t);

         things.send("humidity");
         delay(350);
         t = things.receive();
         System.out.println("humidity " + t);
         }*/
    }
}
