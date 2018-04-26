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



import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ServerLauncher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);
	private static final String SHUTDOWN_ERROR_MESSAGE = "Application shutdown because of unhandled exception, Please check the message trace";
	private static final String SERVLET_CONTEXT_PATH = "/";
	private static final String SERVLET_PATH_SPEC = "/*";
	private final Server server;
	
	
	private void setExceptionHandler(){
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable error) {
				LOGGER.error(SHUTDOWN_ERROR_MESSAGE);
				LOGGER.error(error.getCause().getMessage());
				System.exit(-1);
			}
		});
	}
	
	
	public ServerLauncher(final int port, final String portDescriptor, final int baudRate){
		System.setProperty(ServerCommandProcessor.PORT_DESCRIPTOR_PROPERTY, portDescriptor);
		System.setProperty(ServerCommandProcessor.BAUD_RATE_PROPERTY, String.valueOf(baudRate));
		this.server = new Server(port);
		final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath(SERVLET_CONTEXT_PATH);
		server.setHandler(servletContextHandler);
		servletContextHandler.addServlet(new ServletHolder(new ServerCommandProcessor()), SERVLET_PATH_SPEC);
		this.setExceptionHandler();
	}
	
	public void launch() throws Exception{
		this.server.start();
	}
	
	

}
