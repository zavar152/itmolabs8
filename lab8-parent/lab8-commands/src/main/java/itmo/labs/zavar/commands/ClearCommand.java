package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.db.DbUtils;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandSQLException;

/**
 * Clears the collection. Doesn't require any arguments.
 * 
 * @author Zavar
 * @version 1.0
 */
public class ClearCommand extends Command {

	private ClearCommand() {
		super("clear");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		if (args instanceof String[] && args.length > 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			super.args = args;
			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				try {
					Connection con = null;
					
					try {
						con = env.getDbManager().getConnection();
					} catch (SQLException e2) {
						throw new CommandSQLException("Failed to connect to database!");
					}
					
					PreparedStatement stmt;
					stmt = con.prepareStatement(DbUtils.clearAll());
					stmt.execute();
					con.close();
					((PrintStream) outStream).println("Collection cleared");
				} catch (SQLException e) {
					throw new CommandSQLException(e.getMessage());
				}
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		ClearCommand command = new ClearCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command clears the collection!";
	}
}
