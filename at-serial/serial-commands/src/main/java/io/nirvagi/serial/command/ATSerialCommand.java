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
package io.nirvagi.serial.command;

/**
 * Enum holding the actual serial commands , All the command definitions should
 * be added here. Note : The commands given here are for example purposes
 * 
 * 
 * @author bharath
 *
 */
public enum ATSerialCommand implements SerialCommand {
	/* Zigbee ATI command , Used to print the Serial device hardware info*/
	ZB_ATI("ATI", CommandType.SEND, "Telegesis"), 
	/*Get the Neighbouring table of coordinator*/
	ZB_NTABLE("AT+NTABLE", CommandType.SEND, "Ntable:"),
	/*Wait for an image Query for 45 seconds*/
	ZB_IMGQUERY("IMGQUERY", CommandType.LISTEN, 300, "IMGQUERY:"),
	;
	

	private static final String COMMAND_SEPERATOR = ":";
	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 5;
	private final String commandName;
	private final int commandTimeout;
	private final String commandExceptedOutput;
	private final String commandSeperator;
	private final CommandType commandType;

	private ATSerialCommand(final String commandName, final CommandType commandType, final int commandTimeout,
			final String commandExceptedOutput) {
		this.commandName = commandName;
		this.commandType = commandType;
		this.commandTimeout = commandTimeout;
		this.commandExceptedOutput = commandExceptedOutput;
		this.commandSeperator = COMMAND_SEPERATOR;
	}

	private ATSerialCommand(final String commandName, final CommandType commandType,
			final String commandExceptedOutput) {
		this.commandName = commandName;
		this.commandType = commandType;
		this.commandTimeout = DEFAULT_TIMEOUT_IN_SECONDS;
		this.commandExceptedOutput = commandExceptedOutput;
		this.commandSeperator = COMMAND_SEPERATOR;
	}

	public String getCommandName() {
		return this.commandName;
	}

	public int getCommandTimeout() {
		return this.commandTimeout;
	}

	public String getCommandExpectedOutout() {
		return this.commandExceptedOutput;
	}

	public String getCommandSeperator() {
		return this.commandSeperator;
	}

	public CommandType getCommandType() {
		return this.commandType;
	}

}
