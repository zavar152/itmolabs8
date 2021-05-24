package itmo.labs.zavar.exception;

/**
 * Exception for sql errors in command
 * 
 * @author Zavar
 * @version 1.0
 */
public class CommandSQLException extends CommandException {

	private static final long serialVersionUID = -5590202125739336322L;

	public CommandSQLException(String text) {
		super("SQL Error: " + text);
	}

}
