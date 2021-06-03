package itmo.labs.zavar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import itmo.labs.zavar.commands.base.net.CommandAnswer;

public class ClientWriter {

	public static void write(AsynchronousSocketChannel asyncChannel, ByteBuffer outBuffer, String login, Object[] data) throws InterruptedException, ExecutionException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream ser = new ObjectOutputStream(stream);
		ser.writeObject(new CommandAnswer(new String(outBuffer.array()), login, data));//new String(outBuffer.array()));
		//String str = Base64.getMimeEncoder().encodeToString(stream.toByteArray());
		byte[] str = stream.toByteArray();
		ser.close();
		stream.close();
		asyncChannel.write(ByteBuffer.wrap(str)).get();
	}

}
