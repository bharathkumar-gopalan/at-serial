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
package io.nirvagi.iot.serial.at.command.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUnderflowException;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.nirvagi.iot.serial.SerialInterface;
import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.iot.serial.at.command.CommandResult;
import io.nirvagi.iot.serial.at.command.CommandStatus;
import io.nirvagi.iot.serial.util.StringUtil;
import io.nirvagi.serial.command.SerialCommand;
import io.nirvagi.serial.command.SerialCommand.CommandType;

/**
 * 
 * A task for handling send commands. Send commands are those that are sent to
 * the serial device and a response is expected. The task sends the command to
 * the device and waits on the observable to update , or the event to
 * timeout(whichever happens earlier) The response matching is done by using
 * regular expression
 * 
 * @author bharath
 *
 */
public class SendCommandTask implements Runnable, Observer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendCommandTask.class);
	private static final int BUFFER_MAX_SIZE = 2000;
	private final BlockingQueue<CommandRequest> commandRequestQueue;
	private final BlockingQueue<CommandResult> commandResultQueue;
	// circular Buffer to hold received serial events
	private final Buffer rxBuffer;
	// Serial interface to send and receive commands
	private final SerialInterface serialInterface;

	

	private CommandResult waitForCommandResultUntilTimeout(final String expectedCommandResponse,
			final int commandTimeout) {
		final long commandTimeoutDuration = System.currentTimeMillis() + commandTimeout * 1000;
		final CommandResult commandResult = new CommandResult();
		while (System.currentTimeMillis() < commandTimeoutDuration) {
			try {
				@SuppressWarnings("unchecked")
				final List<String> serialResponse = (List<String>) this.rxBuffer.remove();
				if (StringUtil.hasCommandErred(serialResponse) == true) {
					commandResult.setCommandOutput(serialResponse);
					commandResult.setCommandStatus(CommandStatus.ERROR);
					return commandResult;
				}
				final List<String> matchingOutputString = StringUtil.getMatchingString(serialResponse,
						expectedCommandResponse);
				if (matchingOutputString.isEmpty() == false) {
					commandResult.setCommandOutput(matchingOutputString);
					commandResult.setCommandStatus(CommandStatus.SUCCESS);
					return commandResult;
				}
			} catch (BufferUnderflowException err) {
				continue;
			}
		}
		// If the control flow reaches here it implies that the command has
		// timed out
		commandResult.setCommandStatus(CommandStatus.TIMEOUT);
		commandResult.setCommandOutput(new ArrayList<String>());
		return commandResult;
	}

	/**
	 * Handle a send command . This method is synchronized because at any given
	 * time the system can handle only one command in-flight
	 * 
	 * @param serialCommand
	 *            The serial command to send
	 * @param commandPayload
	 *            The actual command payload in bytes
	 * @return A command result representing the status of the command
	 */
	private synchronized CommandResult sendCommandData(final SerialCommand serialCommand, final byte[] commandPayload) {
		/*
		 * Clear the RX buffer before sending any command , This is to ensure
		 * that we don't parse any stale command output that are left in the
		 * RxQueue
		 */
		this.rxBuffer.clear();
		// Mark the starting time of the command
		final long commandStartTime = System.currentTimeMillis();
		this.serialInterface.write(commandPayload);
		final CommandResult cr = this.waitForCommandResultUntilTimeout(serialCommand.getCommandExpectedOutout(),
				serialCommand.getCommandTimeout());
		// set the command duration
		final double commandDuration = (System.currentTimeMillis() - commandStartTime) / 1000f;
		cr.setCommandDuration(Double.parseDouble(new DecimalFormat(".##").format(commandDuration)));
		return cr;

	}

	private CommandResult sendCommandRequest(final CommandRequest commandRequest) {
		final SerialCommand serialCommand = commandRequest.getCommand();
		final String commandString = StringUtil.buildCommandString(commandRequest);
		LOGGER.debug("Built the command String {}", commandString);
		final byte[] commandPayload = commandString.getBytes();
		LOGGER.debug("Sending the command {}", commandString);
		return this.sendCommandData(serialCommand, commandPayload);
	}

	public SendCommandTask(final SerialInterface serialInterface,
			final BlockingQueue<CommandRequest> commandRequestQueue,
			final BlockingQueue<CommandResult> commandResultQueue) {
		LOGGER.debug("Starting the command Processor task...");
		this.rxBuffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(BUFFER_MAX_SIZE));
		this.serialInterface = serialInterface;
		// Add this task as an observer
		this.serialInterface.addObserver(this);
		this.commandRequestQueue = commandRequestQueue;
		this.commandResultQueue = commandResultQueue;
	}

	public void run() {
		while (true) {
			CommandRequest commandRequest = null;
			CommandResult commandResult = null;
			try {
				commandRequest = this.commandRequestQueue.take();
			} catch (InterruptedException e) {
				/*
				 * bit of a bad design, For this we just handle the exception
				 * and re-throw it as runtime exception . This will be handled
				 * in the Thread's un-handled exception handler(by shutting down
				 * the interface and terminating the program ) Any interruptions
				 * that happen in this task is Bad
				 * 
				 */
				throw new RuntimeException(e);
			}
			if (commandRequest.getCommand().getCommandType() == CommandType.LISTEN) {
				LOGGER.warn("Cannot handle a listen command , Ignoring");
				continue;
			}
			commandResult = this.sendCommandRequest(commandRequest);
			LOGGER.debug("The result is {}", new Gson().toJson(commandResult, CommandResult.class));
			this.commandResultQueue.offer(commandResult);
		}

	}

	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {
		final List<String> outputData = StringUtil.convertDataBytesToString((byte[]) arg);
		if (outputData.isEmpty() == false) {
			LOGGER.debug("Serial event recieved {}", outputData);
			this.rxBuffer.add(outputData);
		}

	}

}
