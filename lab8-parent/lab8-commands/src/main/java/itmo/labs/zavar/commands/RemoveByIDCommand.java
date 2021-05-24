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
import itmo.labs.zavar.exception.CommandPermissionException;
import itmo.labs.zavar.exception.CommandRunningException;
import itmo.labs.zavar.exception.CommandSQLException;

/**
 * Deletes an item from the collection by its id. Requires ID.
 * 
 * @author Zavar
 * @version 1.3
 */
public class RemoveByIDCommand extends Command {

	private RemoveByIDCommand() {
		super("remove_by_id", "id");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException {
		if (args instanceof String[] && args.length != 1 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command requires id of element only!\n" + getUsage());
		} else {
			super.args = args;
			int id;
			try {
				id = Integer.parseInt((String) args[0]);
			} catch (NumberFormatException e) {
				throw new CommandArgumentException("ID must be a number!\n" + getUsage());
			} catch (Exception e) {
				throw new CommandRunningException("Unexcepted error! " + e.getMessage());
			}

			try {
				if (type.equals(ExecutionType.SERVER) | type.equals(ExecutionType.SCRIPT) | type.equals(ExecutionType.INTERNAL_CLIENT)) {
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
					try {
						
						stmt = con.prepareStatement(DbUtils.getOwner(id));
						rs = stmt.executeQuery();
						if(!rs.next()) {
							throw new CommandArgumentException("No such id in the collection!");
						} else {
							if(!rs.getString(1).equals(env.getUser(type.equals(ExecutionType.INTERNAL_CLIENT) ? "internal" : (String) args[args.length-1]))) {
								throw new CommandPermissionException();
							}
						}
						
						stmt = con.prepareStatement(DbUtils.deleteById(id));
						if (stmt.executeUpdate() == 0) {
							((PrintStream) outStream).println("No such element!");
						} else {
							((PrintStream) outStream).println("Element deleted!");
						}
					} catch (CommandPermissionException e) {
						throw new CommandException(e.getMessage());
					} catch (Exception e) {
						throw new CommandRunningException("Unexcepted error! " + e.getMessage());
					} finally {
						if(con != null)
							con.close();
					}
				}
			} catch (SQLException e) {
				throw new CommandSQLException(e.getMessage());
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		RemoveByIDCommand command = new RemoveByIDCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command removes one element from collection by ID!";
	}

}
