package itmo.labs.zavar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import itmo.labs.zavar.commands.base.CommandAnswer;

public class ClientWriter {

	public static void write(AsynchronousSocketChannel asyncChannel, ByteBuffer outBuffer, String login) throws InterruptedException, ExecutionException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream ser = new ObjectOutputStream(stream);
		ser.writeObject(new CommandAnswer(new String(outBuffer.array()), login, null));//new String(outBuffer.array()));
		String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
		ser.close();
		stream.close();
		asyncChannel.write(ByteBuffer.wrap(str.getBytes())).get();
	}

}
