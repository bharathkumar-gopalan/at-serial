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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import io.nirvagi.iot.serial.at.command.CommandRequest;
import io.nirvagi.iot.serial.at.command.CommandResult;
import io.nirvagi.iot.serial.at.command.executor.ATSerialCommandExecutor;
import io.nirvagi.iot.serial.at.command.executor.CommandExecutor;

/**
 * A simple servlet that exposes an Rest-ish interface that allows the user to
 * send and receive serial commands to and from the serial interface . It should
 * be possible to integrate the same with an MQTT based client, for example that
 * would allow users to send and receive the commands remotely .
 * 
 * 
 * @author bharath
 *
 */
public class ServerCommandProcessor extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3794643339436069788L;
	private static final String ACCEPTED_MEDIA_TYPE = "application/json";
	public static final String PORT_DESCRIPTOR_PROPERTY = "device.port";
	public static final String BAUD_RATE_PROPERTY = "device.baudrate";

	// ERROR MESSAGES
	private static final String UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE = "Unsupported media type , only application / json is supported";
	private static final String MALFORMED_REQUEST_ERROR_MESSAGE = "Bad Request , Please check the request data";
	private static final String GENERIC_ERROR_MESSAGE = "An error has occurred while processing your request ";
	

	private CommandExecutor commandExecutor;
	private Gson gson;
	

	private void buildAndCommitResponse(final HttpServletResponse response, int statusCode, final String message)
			throws IOException {
		final PrintWriter out = response.getWriter();
		response.setStatus(statusCode);
		out.write(message);
		out.flush();
	}

	private void checkAndValidateContentTypeHeader(final HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (request.getContentType() == null || request.getContentType().equals(ACCEPTED_MEDIA_TYPE) == false) {
			this.buildAndCommitResponse(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE);
		}
	}
	

	private void processRequest(final HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Validate the content type header
		this.checkAndValidateContentTypeHeader(request, response);
		final StringBuffer sb = new StringBuffer();
		final BufferedReader br = request.getReader();
		String line = null;
		CommandRequest cr = null;

		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		// Convert the request json to Command Request
		try {
			System.out.println("Got the request " + sb.toString());
			cr = this.gson.fromJson(sb.toString(), CommandRequest.class);
			final CommandResult result = this.commandExecutor.execute(cr);
			this.buildAndCommitResponse(response, HttpServletResponse.SC_OK, this.gson.toJson(result));
		} catch (JsonSyntaxException err) {
			this.buildAndCommitResponse(response, HttpServletResponse.SC_BAD_REQUEST, MALFORMED_REQUEST_ERROR_MESSAGE);
		} catch (JsonParseException err) {
			this.buildAndCommitResponse(response, HttpServletResponse.SC_BAD_REQUEST, err.getMessage());
		} catch (Exception err) {
			this.buildAndCommitResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);
		}

	}

	public void init() {
		// Init the command executor
		final String devicePort = System.getProperty(PORT_DESCRIPTOR_PROPERTY);
		final int baudRate = Integer.parseInt(System.getProperty(BAUD_RATE_PROPERTY));
		this.commandExecutor = new ATSerialCommandExecutor(devicePort, baudRate);
		this.gson = new GsonBuilder().registerTypeAdapter(CommandRequest.class, new CommandRequestDesierializer()).create();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.processRequest(request, response);
	}

}
