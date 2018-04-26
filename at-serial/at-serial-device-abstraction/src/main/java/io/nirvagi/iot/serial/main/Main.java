package io.nirvagi.iot.serial.main;

import javax.inject.Inject;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import io.nirvagi.iot.serial.server.ServerLauncher;

/**
 * The main launcher class , Which launches the server process . Airline is used
 * to parse commandline args
 * 
 * @author bharath
 *
 */
@Command(name = "at-agent", description = "The AT command processing agent")
public class Main {
	
	private static final String BAUD_RATE_NOT_SET_ERROR_MESSAGE = "The baud rate is not set , It must be set with the -baud parameter";
	private static final String PD_NOT_SET_ERROR_MESSAGE = "The port descriptor is not set , It must be set with the -pd parameter";
	private static final String PORT_NOT_SPECIFIED_INFO_MESSAGE = "The server port is not specifed, Attempting to use the default port %s";
	private static final int DEFAULT_PORT = 4444;
	
	
	@Option(title="portDescriptor", name={"-pd"}, description="The port descriptor of the serial device")  
	private String portDescriptor;
	@Option(title="serverPort", name={"-port"}, description="The port to start the server")
	private int serverPort;
	@Option(title="baudRate", name={"-baud"}, description="The baud Rate of the serial device")
	private int baudRate;
	
	
	
	@SuppressWarnings("rawtypes")
	@Inject
    private HelpOption helpOption;
	
	public static void main(String args[]) throws Exception{
		final SingleCommand<Main> parser = SingleCommand.singleCommand(Main.class);
		Main main = null;
		try{
		main = parser.parse(args);
		}catch(Exception err){
			System.out.println("invalid option , Please type --help for the details");
			return;
		}
		
		if(main.helpOption.showHelpIfRequested()){
			return;
		}
		
		if(main.baudRate == 0){
			System.err.println(BAUD_RATE_NOT_SET_ERROR_MESSAGE);
			return;
		}
		
		if(main.serverPort == 0){
			System.out.println(String.format(PORT_NOT_SPECIFIED_INFO_MESSAGE, DEFAULT_PORT));
			main.serverPort = DEFAULT_PORT;
		}
		
		if(main.portDescriptor == null || main.portDescriptor.isEmpty()){
			System.err.println(PD_NOT_SET_ERROR_MESSAGE);
			return;
		}
		
		
		
		final ServerLauncher serverLauncher = new ServerLauncher(main.serverPort, main.portDescriptor, main.baudRate);
		serverLauncher.launch();	
	}
	

}
