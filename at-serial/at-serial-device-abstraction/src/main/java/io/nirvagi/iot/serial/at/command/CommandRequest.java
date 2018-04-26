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

import io.nirvagi.serial.command.SerialCommand;

/**
 * A holder representing a command.
 * 
 * @author bharath
 *
 */
public class CommandRequest {
	private SerialCommand command;
	private List<String> commandParameters;
	
	public SerialCommand getCommand() {
		return command;
	}
	public void setCommand(SerialCommand command) {
		this.command = command;
	}
	public List<String> getCommandParameters() {
		return commandParameters;
	}
	public void setCommandParameters(List<String> commandParameters) {
		this.commandParameters = commandParameters;
	}
	
	
	
	

}
