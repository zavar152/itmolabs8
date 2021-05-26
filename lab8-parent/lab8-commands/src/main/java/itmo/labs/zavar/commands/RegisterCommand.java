package itmo.labs.zavar.commands;

import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.db.DbUtils;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandSQLException;

public class RegisterCommand extends Command {

	private RegisterCommand() {
		super("register");
	}

	@SuppressWarnings("resource")
	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream) throws CommandException {
		if (args instanceof String[] && args.length != 0 && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command doesn't require any arguments!\n" + getUsage());
		} else {
			PrintStream pr = ((PrintStream) outStream);
			String password = "", login = "";
			
			if(type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT)) {
				
				Scanner scanner = new Scanner(inStream);
				Console console = System.console();
				if (console != null) {
					
					pr.println("Enter your login:");
					while(login.isEmpty() || login.length() <= 4) {
						login = console.readLine();
						if(login.isEmpty() || login.length() <= 4) {
							pr.println("Login should be longer than 4 characters");
						}
					}
					pr.println("Enter your password:");
					while(password.isEmpty() || password.length() <= 8) {
						password = new String(console.readPassword());
						if(password.isEmpty() || password.length() <= 8) {
							pr.println("Password should be longer than 8 characters");
						}
					}
					password = DigestUtils.md5Hex(password);
					
				} else {
					
					pr.println("Enter your login:");
					while(login.isEmpty() || login.length() <= 4) {
						login = scanner.nextLine();
						if(login.isEmpty() || login.length() <= 4) {
							pr.println("Login should be longer than 4 characters");
						}
					}
					pr.println("Enter your password:");
					while(password.isEmpty() || password.length() <= 8) {
						password = new String(scanner.nextLine());
						if(password.isEmpty() || password.length() <= 8) {
							pr.println("Password should be longer than 8 characters");
						}
					}
					password = DigestUtils.md5Hex(password);
				}
				
				super.args = new String[] {login, password};
			}
			if(type.equals(ExecutionType.SERVER)) {
				login = (String) args[0];
				password = (String) args[1];
			}
			
			if(type.equals(ExecutionType.SERVER) || type.equals(ExecutionType.INTERNAL_CLIENT)) {
				Connection con = null;
				try {
					try {
						con = env.getDbManager().getConnection();
					} catch (SQLException e2) {
						throw new CommandSQLException("Failed to connect to database!");
					}
					PreparedStatement stmt;
					stmt = con.prepareStatement(DbUtils.register());
					stmt.setString(1, login);
					stmt.setString(2, password);
					stmt.executeUpdate();
					pr.println("Registration is finished!");
				} catch (SQLException e) {
					if(e.getMessage().contains("duplicate"))
						pr.println("User is existed! Change your login!");
					else
						throw new CommandSQLException(e.getMessage());
				} catch(Exception e) {
					throw new CommandException(e.getMessage());
				} finally {
					try {
						if(con != null)
							con.close();
					} catch (SQLException e1) {}
				}
			}
		}
	}

	public static void register(HashMap<String, Command> commandsMap) {
		RegisterCommand command = new RegisterCommand();
		commandsMap.put(command.getName(), command);
	}
	
	@Override
	public String getHelp() {
		return "This command is using for registration!";
	}
	
	@Override
	public boolean isAuthorizationRequired() {
		return false;
	}

}
