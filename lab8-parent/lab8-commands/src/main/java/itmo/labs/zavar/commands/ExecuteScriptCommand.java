package itmo.labs.zavar.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.exception.CommandArgumentException;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandRecursionException;
import itmo.labs.zavar.exception.CommandRunningException;

/**
 * Reads and executes the script from the specified file. The script contains
 * commands in the same form as the user enters them interactively. Requires a
 * path to file.
 * 
 * @author Zavar
 * @version 1.9
 */
public class ExecuteScriptCommand extends Command {

	private ExecuteScriptCommand() {
		super("execute_script", "path_to_file");
	}

	@Override
	public void execute(ExecutionType type, Environment env, Object[] args, InputStream inStream, OutputStream outStream)
			throws CommandException {
		if ((args.length > 1 || args.length < 1) && (type.equals(ExecutionType.CLIENT) || type.equals(ExecutionType.INTERNAL_CLIENT))) {
			throw new CommandArgumentException("This command requires one argument!\n" + getUsage());
		} else {
			if (type.equals(ExecutionType.SCRIPT) || type.equals(ExecutionType.INTERNAL_CLIENT)) {
				super.args = args;
				
				File file = new File((String) args[0]);
				
				if (!file.canWrite() || file.isDirectory() || !file.isFile()) {
					throw new CommandRunningException("Error while reading script file!");
				}
				
				if (!file.exists()) {
					throw new CommandRunningException("File not found!");
				}

				if ((env.getHistory().getTempHistory().size() > 1) && (env.getHistory().getTempHistory().contains(getName() + " " + Arrays.toString(args).replace("[", "").replace("]", "")))) {
					throw new CommandRecursionException();
				}
				env.getHistory().addToTemp((getName() + " " + Arrays.toString(args).replace("[", "").replace("]", "")));

				List<String> lines = null;
				try {
					lines = Files.readAllLines(Paths.get((String) args[0]), StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new CommandRunningException("IOException!");
				} catch (Exception e) {
					throw new CommandRunningException("Error while reading script file!");
				}

				for (int i = 0; i < lines.size(); i++) {
					String line = lines.get(i);
					line = line.replaceAll(" +", " ").trim();
					String command[] = line.split(" ");

					if (env.getCommandsMap().containsKey(command[0])) {
						try {
							if (env.getCommandsMap().get(command[0]).isNeedInput()) {
								String to = "";
								int jsonPos = command.length;
								for (int k = 0; k < command.length; k++) {
									if (command[k].contains("{") || command[k].contains("}")) {
										jsonPos = k;
										break;
									}
								}

								JSONObject obj;
								try {
									obj = (JSONObject) new JSONParser().parse(command[jsonPos]);
								} catch (ParseException | ArrayIndexOutOfBoundsException e) {
									throw new CommandRunningException("JSON parsing failed!");
								}

								String[] order = env.getCommandsMap().get(command[0]).getInputOrder(obj.size());
								for (int j = 0; j < order.length; j++) {
									to = to + obj.get(order[j]) + "\n";
								}
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.SCRIPT, env, Arrays.copyOfRange(command, 1, jsonPos), new ReaderInputStream(new StringReader(to), StandardCharsets.UTF_8), outStream);
							} else {
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.SCRIPT, env, Arrays.copyOfRange(command, 1, command.length), inStream, outStream);
							}
						} catch (CommandException e) {
							throw new CommandException(e.getMessage());
						} catch (Exception e) {
							((PrintStream) outStream).println("Script is interrupted! Check your commands and arguments!");
							throw new CommandException(e.getMessage());
						}
					} else {
						((PrintStream) outStream).println("Script is interrupted!");
						throw new CommandException("Unknown command #" + (i + 1) + "! Please, check your script!");
					}
				}
				((PrintStream) outStream).println("Script is completed!");
			} else if(type.equals(ExecutionType.CLIENT)) {
				
				File file = new File((String) args[0]);
				
				if (!file.canWrite() || file.isDirectory() || !file.isFile()) {
					throw new CommandRunningException("Error while reading script file!");
				}
				
				if (!file.exists()) {
					throw new CommandRunningException("File not found!");
				}

				if ((env.getHistory().getTempHistory().size() > 1) && (env.getHistory().getTempHistory().contains(getName() + " " + Arrays.toString(args).replace("[", "").replace("]", "")))) {
					throw new CommandRecursionException();
				}
				env.getHistory().addToTemp((getName() + " " + Arrays.toString(args).replace("[", "").replace("]", "")));

				List<String> lines = null;
				try {
					lines = Files.readAllLines(Paths.get((String) args[0]), StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new CommandRunningException("IOException!");
				} catch (Exception e) {
					throw new CommandRunningException("Error while reading script file!");
				}

				for (int i = 0; i < lines.size(); i++) {
					String line = lines.get(i);
					line = line.replaceAll(" +", " ").trim();
					String command[] = line.split(" ");

					if (env.getCommandsMap().containsKey(command[0])) {
						try {
							if (env.getCommandsMap().get(command[0]).isNeedInput()) {
								String to = "";
								int jsonPos = command.length;
								for (int k = 0; k < command.length; k++) {
									if (command[k].contains("{") || command[k].contains("}")) {
										jsonPos = k;
										break;
									}
								}

								JSONObject obj;
								try {
									obj = (JSONObject) new JSONParser().parse(command[jsonPos]);
								} catch (ParseException | ArrayIndexOutOfBoundsException e) {
									throw new CommandRunningException("JSON parsing failed!");
								}

								String[] order = env.getCommandsMap().get(command[0]).getInputOrder(obj.size());
								for (int j = 0; j < order.length; j++) {
									to = to + obj.get(order[j]) + "\n";
								}
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, jsonPos), new ReaderInputStream(new StringReader(to), StandardCharsets.UTF_8), outStream);
								if(!command[0].equals("execute_script")) {
									String temp = "";
									for(String s : command)
										temp = temp + " " + s;
									temp = temp.substring(1);
									super.args = ArrayUtils.addAll(super.args, temp);
								}
							} else {
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), inStream, outStream);
								if(!command[0].equals("execute_script")) {
									String temp = "";
									for(String s : command)
										temp = temp + " " + s;
									temp = temp.substring(1);
									super.args = ArrayUtils.addAll(super.args, temp);
								}
							}
						} catch (CommandException e) {
							throw new CommandException(e.getMessage());
						} catch (Exception e) {
							((PrintStream) outStream).println("Script is interrupted! Check your commands and arguments!");
							throw new CommandException(e.getMessage());
						}
					} else {
						((PrintStream) outStream).println("Script is interrupted!");
						throw new CommandException("Unknown command #" + (i + 1) + "! Please, check your script!");
					}
				}
			} else if(type.equals(ExecutionType.SERVER)) {
				String[] temp = new String[args.length];
				for (int j = 0; j < args.length; j++) {
					temp[j] = (String) args[j];
				}
				
				List<String> lines = null;
				try {
					lines = Arrays.asList(temp);
				} catch (Exception e) {
					throw new CommandRunningException("Error while reading script file!");
				}

				for (int i = 0; i < lines.size()-1; i++) {
					String line = lines.get(i);
					line = line.replaceAll(" +", " ").trim();
					String command[] = line.split(" ");
					if (env.getCommandsMap().containsKey(command[0])) {
						try {
							if (env.getCommandsMap().get(command[0]).isNeedInput()) {
								String to = "";
								int jsonPos = command.length;
								for (int k = 0; k < command.length; k++) {
									if (command[k].contains("{") || command[k].contains("}")) {
										jsonPos = k;
										break;
									}
								}

								JSONObject obj;
								try {
									obj = (JSONObject) new JSONParser().parse(command[jsonPos]);
								} catch (ParseException | ArrayIndexOutOfBoundsException e) {
									throw new CommandRunningException("JSON parsing failed!");
								}

								String[] order = env.getCommandsMap().get(command[0]).getInputOrder(obj.size());
								for (int j = 0; j < order.length; j++) {
									to = to + obj.get(order[j]) + "\n";
								}
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.SCRIPT, env, ArrayUtils.addAll(Arrays.copyOfRange(command, 1, jsonPos), args[args.length-1]), new ReaderInputStream(new StringReader(to), StandardCharsets.UTF_8), outStream);
							} else {
								env.getHistory().addToGlobal(line);
								env.getCommandsMap().get(command[0]).execute(ExecutionType.SCRIPT, env, ArrayUtils.addAll(Arrays.copyOfRange(command, 1, command.length), args[args.length-1]), inStream, outStream);
							}
						} catch (CommandException e) {
							throw new CommandException(e.getMessage());
						} catch (Exception e) {
							((PrintStream) outStream).println("Script is interrupted! Check your commands and arguments!");
							throw new CommandException(e.getMessage());
						}
					} else {
						((PrintStream) outStream).println("Script is interrupted!");
						throw new CommandException("Unknown command #" + (i + 1) + "! Please, check your script!");
					}
				}
				((PrintStream) outStream).println("Script is completed!");
			}
		}
	}

	/**
	 * Uses for commands registration.
	 * 
	 * @param commandsMap Commands' map.
	 */
	public static void register(HashMap<String, Command> commandsMap) {
		ExecuteScriptCommand command = new ExecuteScriptCommand();
		commandsMap.put(command.getName(), command);
	}

	@Override
	public String getHelp() {
		return "This command runs a script from a file!";
	}

}
