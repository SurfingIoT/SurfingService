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
import org.surfing.device.SerialDevice;

/**
 *
 * @author vsenger
 */
public class TestUSB0 {

    public static void main(String[] args) {
        IoTSurfboard board = null;
        try {
            board = new IoTSurfboard("COM3", 9600);
            System.out.println("Alcohol      :" + board.alcohol());
            System.out.println("Temperature  :" + board.temperature());
            System.out.println("Humidity     :" + board.humidity());
            System.out.println("Light        :" + board.light());
            System.out.println("Potentiometer:" + board.potentiometer());
            System.out.println("Light        :" + board.light());
            board.red(255);
            Kernel.delay(1000);
            board.red(0);
            Kernel.delay(1000);
            board.green(255);
            Kernel.delay(1000);
            board.green(0);
            Kernel.delay(1000);
            board.blue(255);
            Kernel.delay(1000);
            board.blue(0);
            Kernel.delay(1000);
            
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
