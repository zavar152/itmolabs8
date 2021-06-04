package itmo.labs.zavar.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import itmo.labs.zavar.commands.base.net.CommandAnswer;
import javafx.beans.property.SimpleBooleanProperty;

public class ReaderThread implements Runnable{

	private boolean isLogin, isConnected = false;
	private ReadableByteChannel channel;
	private ByteBuffer buf;
	private String data, ans;
	private PrintWriter dataOut;
	private PrintWriter ansOut;
	private static SimpleBooleanProperty check = new SimpleBooleanProperty(false);
	
	public ReaderThread(ReadableByteChannel channel, ByteBuffer buf, Writer pwriter, Writer ansOut) {
		this.channel = channel;
		this.buf = buf;
		this.ansOut = new PrintWriter(ansOut, true);
		dataOut = new PrintWriter(pwriter, true);
	}

	public static SimpleBooleanProperty getConnectedProperty(){
		return check;
	}
	
	@Override
	public void run() {
		isConnected = true;
		check.set(true);
		while (true) {
			try {
				buf.rewind();
				int bytesRead = channel.read(buf);
				buf.rewind();
				byte[] b = new byte[bytesRead];
				for (int i = 0; i < bytesRead; i++) {
					b[i] = buf.get();
				}
				ByteArrayInputStream stream2 = new ByteArrayInputStream(b);//Base64.getMimeDecoder().decode(b));
				ObjectInputStream obj = new ObjectInputStream(stream2);
				CommandAnswer per = (CommandAnswer) obj.readObject();
				if (per.getAnswer().contains("true")) {
					System.out.println("Login successful!");
					ansOut.println("loginDone");
					isLogin = true;
				} else {
					//System.out.println(per.getAnswer());
					ans = per.getAnswer();
					ansOut.println(ans);
					if(per.getData().length != 0) {
						data = ((String)per.getData()[0]);
						dataOut.println(data);
						//String[] res = ((String)per.getData()[0]).split(";");
						/*for(int i = 0; i < res.length; i++) {
							//System.out.println(res[i]);
							data = data + res[i];
						}*/
					}
				}
				buf.flip();
				buf.put(new byte[buf.remaining()]);
				buf.clear();
			} catch (SocketException | NegativeArraySizeException e) {
				isConnected = false;
				check.set(false);
				//e.printStackTrace();
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
