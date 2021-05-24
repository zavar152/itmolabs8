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
						throw new CommandRunningException("Collection is empty!");
					}
					stmt = con.prepareStatement(DbUtils.getAll());
					rs = stmt.executeQuery();
					
					while(rs.next()) {
						pr.println("ID: " + rs.getString("id"));
						pr.println("Owner: " + rs.getString("owner"));
						pr.println("Name: " + rs.getString("name"));
						pr.println("Coordinte X: " + rs.getString("x"));
						pr.println("Coordinte Y: " + rs.getString("y"));
						pr.println("Creation date: " + rs.getString("creationdate"));
						pr.println("Students count: " + rs.getString("studentscount"));
						pr.println("Expelled students: " + rs.getString("expelledstudents"));
						pr.println("Transferred students: " + rs.getString("transferredstudents"));
						pr.println("Form of Education: " + rs.getString("formofeducation"));
						if (rs.getString("adminname") != null) {
							pr.println("Admin's name: " + rs.getString("adminname"));
							pr.println("Admin's passport ID: " + rs.getString("adminpassportid"));
							pr.println("Admin's eye color: " + rs.getString("admineyecolor"));
							pr.println("Admin's hair color: " + rs.getString("adminhaircolor"));
							if (rs.getString("adminnationality") != null) {
								pr.println("Admin's nationality: " + rs.getString("adminnationality"));
							}
							pr.println("Admin's location X: " + rs.getString("adminlocationx"));
							pr.println("Admin's location Y: " + rs.getString("adminlocationy"));
							pr.println("Admin's location Z: " + rs.getString("adminlocationz"));
							pr.println("Admin's location name: " + rs.getString("adminlocationname"));
							pr.println();
						} else {
							pr.println();
						}
					}
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
