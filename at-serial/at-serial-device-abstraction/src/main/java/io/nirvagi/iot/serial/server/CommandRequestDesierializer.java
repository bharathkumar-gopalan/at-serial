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
package io.nirvagi.iot.serial.server;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.serial.command.ATSerialCommand;
import io.nirvagi.serial.command.SerialCommand;

public class CommandRequestDesierializer implements JsonDeserializer<CommandRequest> {
	private static final String NOT_REGISTERED_ERROR_MESSAGE = "The command %s is not registered , Please make sure that it is added to ATSerialCommand and the module is recompiled";

	private List<String> getCommandParams(final JsonArray jsonArray) {
		final List<String> jsonArrayData = new ArrayList<String>();
		if (jsonArray == null) {
			return jsonArrayData;
		}

		final Iterator<JsonElement> jsonArrayIterator = jsonArray.iterator();
		while (jsonArrayIterator.hasNext()) {
			jsonArrayData.add(jsonArrayIterator.next().getAsString());
		}
		return jsonArrayData;
	}

	public CommandRequest deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		final JsonObject object = json.getAsJsonObject();
		SerialCommand command = null;
		final String commandNameString = object.get("command").getAsString();
		try {
			command = ATSerialCommand.valueOf(commandNameString);	
		} catch (IllegalArgumentException err) {
			System.err.println(err.getMessage());
			throw new JsonParseException(String.format(NOT_REGISTERED_ERROR_MESSAGE, commandNameString));
		}
		final CommandRequest cr = new CommandRequest();
		cr.setCommand(command);
		List<String> commandParameters = null;
		final JsonElement commandParamsElement = object.get("commandParameters");
		if (commandParamsElement == null) {
			return cr;
		} else {
			commandParameters = this.getCommandParams(commandParamsElement.getAsJsonArray());
			cr.setCommandParameters(commandParameters);
		}
		return cr;

	}

}
