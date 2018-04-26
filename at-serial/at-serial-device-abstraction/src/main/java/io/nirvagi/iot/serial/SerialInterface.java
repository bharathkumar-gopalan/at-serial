/*---------------------------------------------------------------------------------------------------------
 * Copyright 2018 - Nirvagi project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * \*-------------------------------------------------------------------------------------------------------------------*/
package io.nirvagi.iot.serial;

import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * 
 * A serial interface based out of jSerialComm library . This class represents a
 * low level serial interface .
 * 
 * This class is essentially an observable , Serial data (AT commands) can also
 * be asynchronous . For example a Zigbee device might request for a OTA upgrade
 * at any time. This means that observers have to be notified when data arrives
 * so that an appropriate action can be taken. To recieve events from the device
 * , Observers need to subscribe to this Observable
 * 
 * @author bharath
 *
 */
public class SerialInterface extends Observable {
	private static final String INTERFACE_UNAVAILABLE_ERROR_MESSAGE = "The serial interface %s is not available , Is the device plugged in or is the device busy(being accessed by other program) ?";
	private static final String INTERFACE_DISCONNECTED_ERROR_MESSAGE = "Unable to send the serial command, Looks like the interface has been removed or is not available";
	private static final Logger LOGGER = LoggerFactory.getLogger(SerialInterface.class);
	private final SerialPort serialPort;

	/**
	 * Add a data listener . Please refer to usage samples at
	 * @see<a href=
	 *        "https://github.com/Fazecast/jSerialComm/wiki/Event-Based-Reading-Usage-Example">jSerialComm
	 *        examples</a>
	 * 
	 */
	private void addDataListener() {
		LOGGER.debug("Adding a serial data listener ");
		this.serialPort.addDataListener(new SerialPortDataListener() {

			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
					return;
				}
				byte[] serialDataBytes = new byte[serialPort.bytesAvailable()];
				serialPort.readBytes(serialDataBytes, serialDataBytes.length);
				setChanged();
				notifyObservers(serialDataBytes);
			}
		});
	}

	/**
	 * Build a serial interface
	 * 
	 * @param portDescriptor
	 *            The serial port descriptor (for example on typical nix based
	 *            OS it will be /dev/tty.<DEVICE_NAME>
	 * @param baudrate
	 *            The Baud rate to initialize the serial port to
	 * 
	 */

	// TODO MOdify the code to work on windows OS
	public SerialInterface(final String portDescriptor, final int baudrate) {
		LOGGER.debug("Attempting to initialize the serial port with descriptor {} and baud rate ", portDescriptor,
				baudrate);
		this.serialPort = SerialPort.getCommPort(portDescriptor);
		this.serialPort.setBaudRate(baudrate);
		final boolean isPortOpened = this.serialPort.openPort();
		if (isPortOpened == false) {
			throw new SerialInterfaceException(String.format(INTERFACE_UNAVAILABLE_ERROR_MESSAGE, portDescriptor));
		}
		this.addDataListener();
		/*
		 * Add a shutdown hook to close the serial interface
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				close();
			}
		});
	}

	/**
	 * Write data to the serial port
	 * 
	 * @param data
	 *            The byte array to write
	 */
	public void write(final byte[] data) {
		if (this.serialPort.isOpen()) {
			if (data != null && data.length > 0) {
				this.serialPort.writeBytes(data, data.length);
			} else {
				LOGGER.error("Cannot write a null or zero length data !");
			}
		} else {
			throw new SerialInterfaceException(INTERFACE_DISCONNECTED_ERROR_MESSAGE);
		}
	}

	/**
	 * Close the serial port
	 */
	public void close() {
		if (serialPort.isOpen()) {
			LOGGER.debug("Closing the serial device.....");
			this.serialPort.closePort();
		}
	}

}
