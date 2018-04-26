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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nirvagi.iot.serial.SerialInterface;
import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.iot.serial.at.command.CommandResult;
import io.nirvagi.iot.serial.at.command.observer.ListenCommandObserver;
import io.nirvagi.serial.command.SerialCommand.CommandType;

public class ATSerialCommandExecutor implements CommandExecutor{
	private static final int QUEUE_MAX_SIZE = 10;
	// TODO is a queue needed? Revisit 
	private final BlockingQueue<CommandRequest> commandRequestQueue;
	private final BlockingQueue<CommandResult> commandResultQueue;
	private final SerialInterface serialInterface;
	private static final Logger LOGGER = LoggerFactory.getLogger(ATSerialCommandExecutor.class);
	private static final int THREAD_SLEEP_TIME_IN_MILLIS = 25;
	
	
	
	
	public ATSerialCommandExecutor(final String portDescriptor, final int baudRate) {
		this.commandRequestQueue = new ArrayBlockingQueue<CommandRequest>(QUEUE_MAX_SIZE);
		this.commandResultQueue = new ArrayBlockingQueue<CommandResult>(QUEUE_MAX_SIZE);
		this.serialInterface = new SerialInterface(portDescriptor , baudRate);
		// Start the command processor thread
		new Thread(new SendCommandTask(serialInterface, commandRequestQueue, commandResultQueue)).start();
	}
	
	
	/**
	 * Handle a send command . Do note that this is a blocking operation . There
	 * can be only one command in-flight for a serial device . Otherwise it is
	 * impossible to parse the serial device output to derive a meaningful
	 * result
	 * 
	 * @param commandRequest
	 * 				The command request to execute 
	 * 	
	 * @return
	 * 		The Command result of the operation
	 */
	private synchronized CommandResult handleSendCommand(final CommandRequest commandRequest){
		this.commandRequestQueue.offer(commandRequest);
		try {
			return this.commandResultQueue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * A Listen command handler , Wait until the 
	 * @param commandRequest
	 * @return
	 */
	private CommandResult handleListenCommand(final CommandRequest commandRequest){
		final ListenCommandObserver commandObserver = new ListenCommandObserver(commandRequest);
		final String commandName = commandRequest.getCommand().getCommandName();
		LOGGER.debug("Adding observer for {}", commandName);
		this.serialInterface.addObserver(commandObserver);
		while(commandObserver.hasEventCompleted() == false){
			try {
				Thread.sleep(THREAD_SLEEP_TIME_IN_MILLIS);
			} catch (InterruptedException e) {
				// Dont care about the interruptions 
			}
		}
		LOGGER.debug("Deleting observer for {}", commandName);
		this.serialInterface.deleteObserver(commandObserver);
		return commandObserver.getCommandResult();
			
	}
	

	public CommandResult execute(CommandRequest commandRequest) {
		final CommandType commandType = commandRequest.getCommand().getCommandType();
		CommandResult cr = null;
		switch (commandType) {
		case LISTEN:
			cr = handleListenCommand(commandRequest);
			break;
		case SEND:
			cr = this.handleSendCommand(commandRequest);
			break;
		}
		return cr;
	}
	
	
	
	

}
