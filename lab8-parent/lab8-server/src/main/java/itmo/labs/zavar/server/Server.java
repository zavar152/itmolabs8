package itmo.labs.zavar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itmo.labs.zavar.commands.AddCommand;
import itmo.labs.zavar.commands.AddIfMaxCommand;
import itmo.labs.zavar.commands.AddIfMinCommand;
import itmo.labs.zavar.commands.AverageOfTSCommand;
import itmo.labs.zavar.commands.ClearCommand;
import itmo.labs.zavar.commands.CountGreaterThanTSCommand;
import itmo.labs.zavar.commands.ExecuteScriptCommand;
import itmo.labs.zavar.commands.ExitCommand;
import itmo.labs.zavar.commands.HelpCommand;
import itmo.labs.zavar.commands.HistoryCommand;
import itmo.labs.zavar.commands.InfoCommand;
import itmo.labs.zavar.commands.LoginCommand;
import itmo.labs.zavar.commands.RegisterCommand;
import itmo.labs.zavar.commands.RemoveAnyBySCCommand;
import itmo.labs.zavar.commands.RemoveByIDCommand;
import itmo.labs.zavar.commands.ShowCommand;
import itmo.labs.zavar.commands.UpdateCommand;
import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Command.ExecutionType;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.db.DataBaseManager;
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandPermissionException;

public class Server {
	
	private static HashMap<String, Command> clientsCommandsMap = new HashMap<String, Command>();
	private static HashMap<String, Command> internalCommandsMap = new HashMap<String, Command>();
	private static ConcurrentHashMap<String, String> userTable = new ConcurrentHashMap<String, String>();
	
	private static final Logger rootLogger = LogManager.getLogger(Server.class.getName());
	
	private static DataBaseManager db;
	
	public static DataBaseManager getDbManager() {
		return db;
	}
	
	public static void main(String[] args) {
		
		if(args.length != 2)
		{
			rootLogger.error("You should enter a server port and tunnel mode!");
			System.exit(0);
		}
		
		db = new DataBaseManager(args[1], "s314935", "", "se.ifmo.ru", "studs", 2222, "pg", 2220, 5432);
		Environment[] envs = prepareEnvironments(db);
		Environment clientEnv = envs[0];
		Environment internalEnv = envs[1];
		
		ExecutorService internalServicesExecutor = Executors.newFixedThreadPool(2);
		ForkJoinPool clientReader = ForkJoinPool.commonPool();
		ExecutorService clientExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
		ForkJoinPool clientWriter = ForkJoinPool.commonPool();
		
		ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
		
		try (AsynchronousServerSocketChannel asyncServerChannel = AsynchronousServerSocketChannel.open()) {
			if (asyncServerChannel.isOpen()) {
				asyncServerChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4096*4);
				asyncServerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				asyncServerChannel.bind(new InetSocketAddress(Integer.parseInt(args[0])));
				rootLogger.info("Waiting for connections ...");
				
				internalServicesExecutor.submit(() -> {
					Scanner scan = new Scanner(System.in);
					Logger internalClientLogger = LogManager.getLogger("internal");
					while (true) {
						try {
							String input = scan.nextLine();
							input = input.replaceAll(" +", " ").trim();
							internalClientLogger.info(input);
							String command[] = input.split(" ");

							if (command[0].equals("exit")) {
								internalEnv.getCommandsMap().get(command[0]).execute(ExecutionType.INTERNAL_CLIENT, internalEnv, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
								internalServicesExecutor.shutdownNow();
								clientReader.shutdownNow();
								System.exit(0);
							}
							
							if (internalEnv.getCommandsMap().containsKey(command[0])) {
								try {
									Command c = internalEnv.getCommandsMap().get(command[0]);
									if(c.isAuthorizationRequired() && !userTable.containsKey("internal")) {
										throw new CommandPermissionException();
									} else {
										internalEnv.getHistory().addToGlobal(input);
										c.execute(ExecutionType.INTERNAL_CLIENT, internalEnv, Arrays.copyOfRange(command, 1, command.length), System.in, System.out);
										internalEnv.getHistory().clearTempHistory();	
									}
								} catch (CommandException e) {
									rootLogger.error(e.getMessage());
									internalEnv.getHistory().clearTempHistory();
								}
							} else {
								rootLogger.error("Unknown command! Use help.");
							}
						} catch (Exception e) {
							if (!scan.hasNextLine()) {
								rootLogger.warn("Inputing is closed! Server is closing...");
								internalServicesExecutor.shutdownNow();
								clientReader.shutdownNow();
								scan.close();
								System.exit(0);
							} else {
								e.printStackTrace();
								rootLogger.error("Unexcepted error!");
							}
						}
					}
				});
				
				ClientUpdater updater = new ClientUpdater(clients, db.getConnection());
				internalServicesExecutor.execute(updater);
				
				while (true) {

					Future<AsynchronousSocketChannel> asynchFuture = asyncServerChannel.accept();

					try {
						final AsynchronousSocketChannel asyncChannel = asynchFuture.get();
						ClientHandler worker = new ClientHandler(asyncChannel, clientEnv, clientExecutor, clientWriter);
						clientReader.submit(worker);
						clients.add(worker);
					} catch (InterruptedException | ExecutionException ex) {
						rootLogger.error(ex);
						rootLogger.error("\n Server is shutting down ...");
						internalServicesExecutor.shutdownNow();
						clientReader.shutdown();
						while (!clientReader.isTerminated());
						break;
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			} else {
				rootLogger.error("The asynchronous server-socket channel cannot be opened!");
			}
		} catch (IOException | SQLException ex) {
			rootLogger.error(ex);
		}
	}
	
	private static Environment[] prepareEnvironments(DataBaseManager db) {
		
		HelpCommand.register(clientsCommandsMap);
		ShowCommand.register(clientsCommandsMap);
		ExecuteScriptCommand.register(clientsCommandsMap);
		ClearCommand.register(clientsCommandsMap);
		InfoCommand.register(clientsCommandsMap);
		AddCommand.register(clientsCommandsMap);
		RemoveByIDCommand.register(clientsCommandsMap);
		HistoryCommand.register(clientsCommandsMap);
		RemoveAnyBySCCommand.register(clientsCommandsMap);
		AverageOfTSCommand.register(clientsCommandsMap);
		CountGreaterThanTSCommand.register(clientsCommandsMap);
		AddIfMaxCommand.register(clientsCommandsMap);
		AddIfMinCommand.register(clientsCommandsMap);
		UpdateCommand.register(clientsCommandsMap);
		RegisterCommand.register(clientsCommandsMap);
		LoginCommand.register(clientsCommandsMap);
		
		internalCommandsMap.putAll(clientsCommandsMap);
		ExitCommand.register(internalCommandsMap);
		
		return new Environment[] { new Environment(userTable, db, clientsCommandsMap),  new Environment(userTable, db, internalCommandsMap)};
	}
}