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
package io.nirvagi.iot.serial.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.nirvagi.iot.serial.at.command.CommandRequest;

public class StringUtil {
	private static final String OUTPUT_RESULT_DELIMITER = "\n";
	private static final String COMMAND_ERROR_STRING = "ERROR";
	// Zigbee typically sends Nacks for certain commands in case of an error  
	private static final String NACK_STRING = "NACK";
	private static final String EXPECTED_OUTPUT_FORMAT = "%s.+";
	private static final String UNPACKING_DELIM = "~";
	private static final String COMMAND_PARAM_SEPERATOR = ",";
	// All serial command should terminate with a CR , else command wont be sent!
	private static final String COMMAND_TERMINATION_CHAR = "\r";
	
	
	public static  List<String> convertDataBytesToString(final byte[] data) {
		final String commandResult = new String(data);
		final List<String> outputDataList = new ArrayList<String>();
		if (commandResult != null && commandResult.isEmpty() == false) {
			for (String resultFragment : commandResult.split(OUTPUT_RESULT_DELIMITER)) {
				if (resultFragment.trim().isEmpty() == false) {
					// Convert the result into upper case , So that there are no
					// issues while pattern matching
					// Also remove any CR present in the output
					resultFragment = resultFragment.replaceAll("\\r", "").toUpperCase();
					outputDataList.add(resultFragment);
				}
			}
		}
		return outputDataList;
	}
	
	
	public static String unpackList(final List<String> outputData){
		String unpackedData = "";
		for(String s : outputData){
			unpackedData += UNPACKING_DELIM + s.toUpperCase();
		}
		return unpackedData.substring(1);
	}
	
	
	
	public static List<String> getMatchingString(final List<String> outputData, final String expectedOutput) {
		final String unpackedString = unpackList(outputData);
		final String regex = String.format(EXPECTED_OUTPUT_FORMAT, expectedOutput.toUpperCase());
		String matchedString = "";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(unpackedString);
		while (matcher.find()) {
			matchedString = matcher.group(0);
		}
		return matchedString.isEmpty() ? new ArrayList<String>(): Arrays.asList(matchedString.split(UNPACKING_DELIM));
	}

	
	
	public static boolean hasCommandErred(final List<String> outputString) {
		final String firstLine = outputString.get(0);
		return (firstLine.contains(COMMAND_ERROR_STRING)|| firstLine.contains(NACK_STRING)) ? true : false;
	}
	
	public static String buildCommandString(final CommandRequest commandRequest) {
		final String commandName = commandRequest.getCommand().getCommandName();
		final String commandSeperator = commandRequest.getCommand().getCommandSeperator().trim();
		String commandParamString = "";
		final List<String> commandParams = commandRequest.getCommandParameters();
		if (commandParams != null) {
			for (String commandParam : commandRequest.getCommandParameters()) {
				commandParamString += COMMAND_PARAM_SEPERATOR + commandParam;
			}
			commandParamString = commandParamString.substring(1);
		} else {
			commandParamString = "";
		}
		final String commandString = commandName + commandSeperator + commandParamString + COMMAND_TERMINATION_CHAR;
		return commandString;
	}
	
	public static void main(String args[]){
		System.out.println(StringUtil.getMatchingString(Arrays.asList(new String[]{"IMGQUERY:D6D6,09,00,1039,0203,01045700,02"}), "IMGQUERY").size());
	}

}
