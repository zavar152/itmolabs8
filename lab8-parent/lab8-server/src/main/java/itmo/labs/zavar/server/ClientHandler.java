package itmo.labs.zavar.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.commands.base.net.CommandPackage;

public class ClientHandler implements Callable<String> {

	private AsynchronousSocketChannel asyncChannel;
	private Environment clientEnv;
	private ExecutorService clientExecutor;
	private ForkJoinPool clientWriter;
	private Logger logger = LogManager.getLogger(ClientHandler.class.getName());
	private ArrayList<Object> outputs = new ArrayList<Object>();
	private String login, host;
	private Semaphore semaphore;
	
	public ClientHandler(AsynchronousSocketChannel asyncChannel, Environment clientEnv, ExecutorService clientExecutor, ForkJoinPool clientWriter) {
		this.asyncChannel = asyncChannel;
		this.clientEnv = clientEnv;
		this.clientExecutor = clientExecutor;
		this.clientWriter = clientWriter;
		semaphore = new Semaphore(1, true);
	}

	@Override
	public String call() throws Exception {
		host = asyncChannel.getRemoteAddress().toString().replace("/", "");
		logger.info("Incoming connection from: " + host);

		final ByteBuffer buffer = ByteBuffer.wrap(new byte[4096 * 4]);

		while (asyncChannel.read(buffer).get() != -1) {
			try {
				CommandPackage per = ClientReader.read(buffer);
				login = per.getLogin();
				logger.info("Command from " + host + (per.getLogin() != null ? " (user - " + per.getLogin() + "): " : ": ") + per.getName());
				
				Future<ByteBuffer> futureOutBuffer = clientExecutor.submit(() -> {
					return ClientCommandExecutor.executeCommand(per, clientEnv, host);
				});

				login = clientEnv.getUser(host);
				
				ByteBuffer outBuffer = futureOutBuffer.get();
				
				writeToClient(outBuffer);
				
				logger.info("Send command's output to " + host);
				
				buffer.flip();
				buffer.put(new byte[buffer.remaining()]);
				buffer.clear();

			} catch (Exception e) {
				logger.error("Error while handling " + host);
			}
		}

		asyncChannel.close();
		clientEnv.removeUser(host);
		logger.info("Client " + host + " was successfully served");
		return host;
	}
	
	public boolean isOpen() {
		return asyncChannel.isOpen();
	}
	
	public void addOutput(Object buf) {
		outputs.add(buf);
	}
	
	public void writeToClient(ByteBuffer outBuffer, Object ... buf) {
		clientWriter.submit(() -> {
			try {
				semaphore.acquire();
				ClientWriter.write(asyncChannel, outBuffer, login, buf);
			} catch (InterruptedException | ExecutionException | IOException e) {
				e.printStackTrace();
				logger.error("Error while writing output to " + host);
			}
			semaphore.release();
		});
	}
	
	public String getLogin() {
		return login;
	}
	
	public String getHost() {
		return host;
	}
}
