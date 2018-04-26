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
package io.nirvagi.iot.serial.at.command.observer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.iot.serial.at.command.CommandResult;
import io.nirvagi.iot.serial.at.command.CommandStatus;
import io.nirvagi.iot.serial.util.StringUtil;
import io.nirvagi.serial.command.SerialCommand;

/**
 * Handler for a listen command , Certain types of AT commands are asynchronous
 * ,Which could be triggered at any given time , For example an image query (in
 * zigbee world). Classes need to implement the handle method
 * 
 * @author bharath
 *
 */
public class ListenCommandObserver implements CommandObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListenCommandObserver.class);
	private final SerialCommand serialCommand;
	private final String commandExpectedOutput;
	private final CommandResult commandResult;
	private final long commandStartTimeMillis;
	private boolean hasCommandSucceeded;
	
	
	
	
	private CommandResult buildInitialCommandResult(final SerialCommand command){
		final CommandResult cr = new CommandResult();
		cr.setCommandDuration(command.getCommandTimeout());
		cr.setCommandOutput(new ArrayList<String>());
		cr.setCommandStatus(CommandStatus.TIMEOUT);
		return cr;
	}
	
	private double computeTimeFromCommandStart(){
		final double duration =  (System.currentTimeMillis() - this.commandStartTimeMillis)/1000f;
		return Double.parseDouble(new DecimalFormat(".##").format(duration));
	}
	
	private boolean hasCommandTimedout(){
		return System.currentTimeMillis() > (this.commandStartTimeMillis + serialCommand.getCommandTimeout() *1000);
	}
	
	

	public ListenCommandObserver(final CommandRequest commandRequest) {
		
		LOGGER.debug("Listening for the command {}", commandRequest);
		this.serialCommand = commandRequest.getCommand();
		this.commandExpectedOutput = StringUtil.buildCommandString(commandRequest).toUpperCase().trim();
		this.hasCommandSucceeded = false;
		this.commandStartTimeMillis = System.currentTimeMillis();
		this.commandResult = this.buildInitialCommandResult(serialCommand);
		LOGGER.debug("Waiting for the expected output {}", commandExpectedOutput);
	}

	public void update(Observable o, Object arg) {
		final List<String> commandResponse = StringUtil.convertDataBytesToString((byte[]) arg);
		final String unpackedOutput = StringUtil.unpackList(commandResponse).toUpperCase().trim();
		if(unpackedOutput.contains(this.commandExpectedOutput)){
			LOGGER.debug("Got the matched command output {}", commandResponse);
			this.commandResult.setCommandStatus(CommandStatus.SUCCESS);
			this.commandResult.setCommandOutput(commandResponse);
			this.commandResult.setCommandDuration(this.computeTimeFromCommandStart());			
			this.hasCommandSucceeded = true;
		}
	}
	
	
	
	public CommandResult getCommandResult() {
		return this.commandResult;
	}

	public boolean hasEventCompleted() {
		return this.hasCommandTimedout() || this.hasCommandSucceeded;
	}
	
	
	
}
