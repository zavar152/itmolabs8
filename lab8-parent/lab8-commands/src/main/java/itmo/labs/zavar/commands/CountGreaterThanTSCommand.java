package itmo.labs.zavar.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.db.DbUtils;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRunningException;
import itmo.labs.zavar.exception.CommandSQLException;

/**
 * Outputs the number of elements whose transferredStudents field value is
 * greater than the specified value. Requires transferred students.
 * 
 * @author Zavar
 * @version 1.2
 */
public class CountGreaterThanTSCommand extends Command {

	private CountGreaterThanTSCommand() {
		super("count_greater_than_transferred_students", "transferredStudents");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		if (args instanceof String[] && args.length != 1 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command requires one argument!\n" + getUsage());
		} else {
			super.args = args;
			long tr;
			try {
				tr = Long.parseLong((String) args[0]);
			} catch (NumberFormatException e) {
				throw new CommandArgumentException("transferredStudents shold be a long type!");
			} catch (Exception e) {
				throw new CommandRunningException("Unexcepted error! " + e.getMessage());
			}

			if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
				try {
					Connection con = null;
					
					try {
						con = env.getDbManager().getConnection();
					} catch (SQLException e2) {
						throw new CommandSQLException("Failed to connect to database!");
					}
					
					PreparedStatement stmt;
					stmt = con.prepareStatement(DbUtils.getCount());
					ResultSet rs = stmt.executeQuery();
					rs.next();
					if (rs.getInt(1) == 0) {
						con.close();
						throw new CommandRunningException("Collection is empty!");
					}

					stmt = con.prepareStatement(DbUtils.countGreaterThanTs(tr));
					rs = stmt.executeQuery();
					rs.next();
					long count = rs.getLong(1);
					((PrintStream) outStream).println("Count of elements: " + count);
					con.close();
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
		CountGreaterThanTSCommand command = new CountGreaterThanTSCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command counts a number of elements which transferred students count is greater than argument!";
	}

}
