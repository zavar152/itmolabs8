package itmo.labs.zavar.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;

import itmo.labs.zavar.commands.base.net.CommandAnswer;

public class ReaderThread implements Runnable{

	private boolean isLogin, isConnected = false;
	private ReadableByteChannel channel;
	private ByteBuffer buf;
	
	public ReaderThread(ReadableByteChannel channel, ByteBuffer buf) {
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
					//System.out.println(per.getLogin());
					if(per.getData().length != 0) {
						String[] res = ((String)per.getData()[0]).split(";");
						for(int i = 0; i < res.length; i++) {
							System.out.println(res[i]);
						}
					}
				}
				buf.flip();
				buf.put(new byte[buf.remaining()]);
				buf.clear();
			} catch (SocketException | NegativeArraySizeException e) {
				isConnected = false;
				break;
			} catch (Exception e) {
				e.printStackTrace();
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
