/*---------------------------------------------------------------------------------------------------------
 * Copyright 2016 - Nirvagi project
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
package io.nirvagi.iot.serial.at.command;

import java.util.List;

/**
 * A holder representing a command result . A command result has a command
 * output(List representing a series of command result) , With command status
 * and the time taken by the command to execute
 * 
 * @author bharath
 *
 */

public class CommandResult {
	private CommandStatus commandStatus;
	// Total time taken by the command to execute 
	private double commandDuration;
	// The command result 
	private List<String> commandOutput;
	
	public double getCommandDuration() {
		return commandDuration;
	}
	public void setCommandDuration(double commandDuration) {
		this.commandDuration = commandDuration;
	}
	public List<String> getCommandOutput() {
		return commandOutput;
	}
	public void setCommandOutput(List<String> commandOutput) {
		this.commandOutput = commandOutput;
	}
	public CommandStatus getCommandStatus() {
		return commandStatus;
	}
	public void setCommandStatus(CommandStatus commandStatus) {
		this.commandStatus = commandStatus;
	}
	
	

}
