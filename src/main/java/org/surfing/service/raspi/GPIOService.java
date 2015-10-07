package org.surfing.service.raspi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.surfing.Kernel;
import org.surfing.Service;

public final class GPIOService extends Service {
    Kernel kernel;
    private boolean alternate;

    public void initialize() {

    }

    static void blink(GpioPinDigitalOutput myLed, int nTimes, int delay) throws Exception {
        for (int x = 0; x < nTimes; x++) {
            myLed.setState(true);
            Thread.sleep(delay);
            myLed.setState(false);
            Thread.sleep(delay);
        }
    }

    @Override
    public void start() {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00,
                PinPullResistance.PULL_DOWN); //GPIO_00 = 17
        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (event.getState() == PinState.LOW && !alternate) {
                    kernel.changeMode();
                }
                alternate = !alternate;

            }

        });
    }

    @Override
    public void stop() {
        GpioFactory.getInstance().shutdown();
    }

    @Override
    public void run() {
        //no run method, using async interruptions
    }
}
