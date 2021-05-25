package itmo.labs.zavar.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;

import itmo.labs.zavar.commands.base.CommandAnswer;

public class WriterThread implements Runnable{

	private boolean isLogin, isConnected = false;
	private ReadableByteChannel channel;
	private ByteBuffer buf;
	
	public WriterThread(ReadableByteChannel channel, ByteBuffer buf) {
		this.channel = channel;
		this.buf = buf;
	}

	@Override
	public void run() {
		isConnected = true;
		while (true) {
			try {
				buf.rewind();
				int bytesRead = channel.read(buf);
				buf.rewind();
				byte[] b = new byte[bytesRead];
				for (int i = 0; i < bytesRead; i++) {
					b[i] = buf.get();
				}
				ByteArrayInputStream stream2 = new ByteArrayInputStream(Base64.getMimeDecoder().decode(b));
				ObjectInputStream obj = new ObjectInputStream(stream2);
				CommandAnswer per = (CommandAnswer) obj.readObject();
				if (per.getAnswer().contains("true")) {
					System.out.println("Login successful!");
					isLogin = true;
				} else {
					System.out.println(per.getAnswer());
					System.out.println(per.getLogin());
				}
				buf.flip();
				buf.put(new byte[buf.remaining()]);
				buf.clear();
			} catch (Exception e) {
				e.printStackTrace();
				isConnected = false;
				break;
			}
		}
	}
	
	public boolean isLogin() {
		return isLogin;
	}
	
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	
	public boolean isConnected() {
		return isConnected;
	}

}
