package itmo.labs.zavar.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import itmo.labs.zavar.client.util.ClientState;
import itmo.labs.zavar.client.util.CommandState;
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
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.exception.CommandPermissionException;
import javafx.beans.property.SimpleBooleanProperty;

public class Client {
	
	private HashMap<String, Command> commandsMap = new HashMap<String, Command>();
	private String login, password; 
	private PipedInputStream dpin;
	private PipedOutputStream pout;
	private PipedInputStream apin;
	private PipedOutputStream apout;
	private Writer pwriter;
	private Writer ansWriter;
	private Environment env;
	private InputStream is;
	private OutputStream os;
	private Writer writer;
	private PrintWriter out;
	private ReadableByteChannel channel;
	private ByteBuffer buf;
	private ReaderThread rdThread;
	private Thread thr;
	private Socket socket = null;
	private boolean connected, lastSent = false;
	private String args[];
	private ClientState clientState;
	private CommandState commandState;
	private String lastError = "";
	
	public Client(String args[]) {
		this.args = args;
		HelpCommand.register(commandsMap);
		ShowCommand.register(commandsMap);
		ExecuteScriptCommand.register(commandsMap);
		ClearCommand.register(commandsMap);
		InfoCommand.register(commandsMap);
		AddCommand.register(commandsMap);
		RemoveByIDCommand.register(commandsMap);
		HistoryCommand.register(commandsMap);
		RemoveAnyBySCCommand.register(commandsMap);
		AverageOfTSCommand.register(commandsMap);
		CountGreaterThanTSCommand.register(commandsMap);
		AddIfMaxCommand.register(commandsMap);
		AddIfMinCommand.register(commandsMap);
		UpdateCommand.register(commandsMap);
		ExitCommand.register(commandsMap);
		RegisterCommand.register(commandsMap);
		LoginCommand.register(commandsMap);
		
		env = new Environment(commandsMap);
	}
	
	public SimpleBooleanProperty getConnectedProperty() {
		return ReaderThread.getConnectedProperty();
	}
	
	public PipedInputStream getDataInput() {
		return dpin;
	}

	public PipedInputStream getAnswerInput() {
		return apin;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public ClientState getClientState() {
		return clientState;
	}
	
	public CommandState getLastCommandState() {
		return commandState;
	}
	
	public boolean isLastCommandSent() {
		return lastSent;
	}
	
	public void shutdown() throws IOException {
		if(socket != null && !socket.isClosed()) {
			socket.close();
		}
	} 
	
	public void close() throws IOException {
		if(socket != null && !socket.isClosed()) {
			connected = false;
			socket.close();
		}
	}
	
	public void connect() throws InterruptedException, IOException {
		if (!connected) {
			clientState = ClientState.CONNECTING;
			while (!connected) {
				try {
					socket = new Socket(args[0], Integer.parseInt(args[1]));
					connected = true;
				} catch (ConnectException e1) {
					Thread.sleep(2000);
				} catch (UnknownHostException e) {
					clientState = ClientState.HOST_ERROR;
					return;
				} catch (Exception e) {
					clientState = ClientState.ERROR;
					return;
				}
			}
			clientState = ClientState.CONNECTED;

			dpin = new PipedInputStream();
			pout = new PipedOutputStream(dpin);
			pwriter = new OutputStreamWriter(pout, StandardCharsets.US_ASCII);

			apin = new PipedInputStream();
			apout = new PipedOutputStream(apin);
			ansWriter = new OutputStreamWriter(apout, StandardCharsets.US_ASCII);
			
			is = socket.getInputStream();
			os = socket.getOutputStream();
			writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
			out = new PrintWriter(writer, true);
			channel = Channels.newChannel(is);
			buf = ByteBuffer.allocateDirect(4096 * 4);
			rdThread = new ReaderThread(channel, buf, pwriter, ansWriter);
			thr = new Thread(rdThread);
			thr.start();
		}
	}
	
	public void executeCommand(String input, InputStream in, OutputStream lOut) throws InterruptedException, IOException, CommandException {

		try {
			input = input.replaceAll(" +", " ").trim();
			String command[] = input.split(" ");

			if (command[0].equals("exit")) {
				commandsMap.get(command[0]).execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), in, lOut);
				close();
			}

			if (!rdThread.isConnected()) {
				throw new SocketException();
			}

			if (env.getCommandsMap().containsKey(command[0])) {
				try {
					Command c = env.getCommandsMap().get(command[0]);
					if (c.isAuthorizationRequired() && !rdThread.isLogin()) {
						throw new CommandPermissionException();
					} else {
						env.getHistory().addToGlobal(input);
						if (!rdThread.isConnected()) {
							throw new SocketException();
						}
						c.execute(ExecutionType.CLIENT, env, Arrays.copyOfRange(command, 1, command.length), in, lOut);
						env.getHistory().clearTempHistory();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						ObjectOutputStream ser = new ObjectOutputStream(stream);
						ser.writeObject(c.getPackage(login, password));
						String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
						out.println(str);
						ser.close();
						stream.close();
						if (c.getName().equals("login")) {
							login = (String) c.getArgs()[0];
							password = (String) c.getArgs()[1];
						}
						commandState = CommandState.DONE;
					}
				} catch (CommandException e) {
					env.getHistory().clearTempHistory();
					lastError = e.getMessage();
					System.err.println(e.getMessage());
					commandState = CommandState.ERROR;
					throw new CommandException(e.getMessage());
				}
			} else {
				System.err.println("Unknown command! Use help.");
				commandState = CommandState.UNSTATED;
			}
		} catch (SocketException | NegativeArraySizeException e) {
			clientState = ClientState.SERVER_UNAVAILABLE;
			connected = false;
			rdThread.setLogin(false);
			commandState = CommandState.SERVER_UNAVAILABLE;
			return;
		} catch (Exception e) {
			if(e instanceof CommandException) {
				throw new CommandException(e.getMessage());
			}
			e.printStackTrace();
			clientState = ClientState.ERROR;
			commandState = CommandState.ERROR;
			return;
		}
	}
	
	public void reconnect(boolean afterLostConnection) throws InterruptedException, IOException {
		if(afterLostConnection)
			connected = false;
		while (!connected) {
			try {
				socket = new Socket(args[0], Integer.parseInt(args[1]));
				connected = true;
			} catch (ConnectException e1) {
				Thread.sleep(2000);
			} catch (Exception e1) {
				clientState = ClientState.ERROR;
				return;
			}
		}

		dpin = new PipedInputStream();
		pout = new PipedOutputStream(dpin);
		pwriter = new OutputStreamWriter(pout, StandardCharsets.US_ASCII);

		apin = new PipedInputStream();
		apout = new PipedOutputStream(apin);
		ansWriter = new OutputStreamWriter(apout, StandardCharsets.US_ASCII);
		
		is = socket.getInputStream();
		os = socket.getOutputStream();
		writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
		out = new PrintWriter(writer, true);
		channel.close();
		channel = Channels.newChannel(is);
		rdThread = new ReaderThread(channel, buf, pwriter, ansWriter);
		thr = new Thread(rdThread);
		thr.start();

		clientState = ClientState.CONNECTED;
	}

	public String getLastError() {
		return lastError;
	}
}