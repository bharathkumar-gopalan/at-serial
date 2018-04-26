package io.nirvagi.serial.command;

/**
 * 
 * An interface representing a serial AT command . AT commands are typically as
 * follows
 * 
 * ATS8=25(An example SIM 900 AT command that sets the number of seconds to wait
 * for command dial modifier encountered in dial string)
 * 
 * For more examples of the AT commands
 * 
 * @see <a href="https://www.espruino.com/datasheets/SIM900_AT.pdf">SIM 9000
 *      commands</a>
 * @see <a href=
 *      "https://www.silabs.com/documents/public/reference-manuals/TG-ETRXn-Commands.pdf">Zigbee
 *      AT commands</a>
 * 
 * @author Bharath Kumar Gopalan
 *
 */
public interface SerialCommand {

	/**
	 * Enum representing the command type , The command can be either a send or a
	 * listen command - for example certain zigbee commands are sent from the
	 * device side to the coordinator(In this case a USB serial device) and we
	 * need to listen to them
	 *
	 */

	public enum CommandType {
		SEND, LISTEN;

	}

	/**
	 * Get the AT command name
	 * 
	 * @return The actual command name (AT+N, for example)
	 */
	public String getCommandName();

	/**
	 * Every command has an associated time before it should return a result ,
	 * else the command execution will be considered as a timeout
	 * 
	 * @return The command timeout duration in seconds
	 */

	public int getCommandTimeout();

	/**
	 * Get the command expected output , This could be a simple string like OK,
	 * Or possibly a regular expression
	 * 
	 * @return String containing the command expected output
	 */

	public String getCommandExpectedOutout();

	/**
	 * Get the command Seperator. For example zigbee commands are seperated by a ':',
	 * SIM900 commands by  a ? 
	 * 
	 * @return
	 * 	The command seperator 
	 */
	public String getCommandSeperator();
	
	
	/**
	 * Get the command type (LISTEN command or SEND command) 
	 * 
	 * @return
	 * 	The command type
	 */
	public CommandType getCommandType();

}
