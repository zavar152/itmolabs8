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
import itmo.labs.zavar.exception.CommandSQLException;

/**
 * Outputs all elements of the collection in a string representation to the
 * standard output stream. Doesn't require any arguments.
 * 
 * @author Zavar
 * @version 1.3
 */
public class ShowCommand extends Command {

	private ShowCommand() {
		super("show");
	}
	
	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		PrintStream pr = ((PrintStream) outStream);
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
					stmt = con.prepareStatement(DbUtils.getCount());
					ResultSet rs = stmt.executeQuery();
					rs.next();
					if (rs.getInt(1) == 0) {
						con.close();
						throw new CommandException("/-/");
					}
					stmt = con.prepareStatement(DbUtils.getAll());
					rs = stmt.executeQuery();
					String res = "";
					
					while (rs.next()) {
						res = "";
						for (int j = 1; j <= 19; j++) {
							if(j == 3 || j >= 10) {
								res = res + rs.getString(j) + ",";
							} else {
								res = res + rs.getString(j) + ";";
							}
						}
						res = res.substring(0, res.length()-1);
						pr.println(res);
					}
					pr.print("/-/");
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
		ShowCommand command = new ShowCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command shows all elements of collection!";
	}
}
