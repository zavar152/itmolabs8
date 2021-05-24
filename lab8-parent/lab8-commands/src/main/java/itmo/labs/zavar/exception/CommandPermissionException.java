package itmo.labs.zavar.exception;

public class CommandPermissionException extends CommandException {

	private static final long serialVersionUID = 8195743792305238146L;

	public CommandPermissionException() {
		super("You don't have permissions!");
	}

}
