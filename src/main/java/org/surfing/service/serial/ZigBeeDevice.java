package org.surfing.service.serial;

import gnu.io.CommPortIdentifier;

/**
 *
 * @author vsenger
 */
public class ZigBeeDevice extends SerialDevice {

    public ZigBeeDevice(CommPortIdentifier portId, int baudRate) {
        super(portId, baudRate);
    }

}
