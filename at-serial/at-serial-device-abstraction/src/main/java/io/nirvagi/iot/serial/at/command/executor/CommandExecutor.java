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

import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.iot.serial.at.command.CommandResult;
/**
 * 
 * An interface representing a command handler .
 * @author bharath
 *
 */
public interface CommandExecutor {
	
	/**
	 * Send a command request to the interface . In case of a serial device ,
	 * This typically can represent a blocking operation (Send command and wait
	 * for the outcome )
	 * 
	 * @param commandRequest
	 * 		The command request to execute
	 * @return
	 * 		the command result
	 */
	public CommandResult execute(final CommandRequest commandRequest);
	
	

}
