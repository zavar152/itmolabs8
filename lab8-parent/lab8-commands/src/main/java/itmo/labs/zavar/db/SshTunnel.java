package itmo.labs.zavar.db;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshTunnel {

	private JSch jsch;
	private Session session;
	private String remoteHost;
	private int localPort;
	private int remotePort;
	
	public SshTunnel(String sshUser, String sshPassword, String sshHost, int sshPort, String remoteHost, int localPort, int remotePort) throws JSchException {
		this.remoteHost = remoteHost;
		this.localPort = localPort;
		this.remotePort = remotePort;
		jsch = new JSch();
		session = jsch.getSession(sshUser, sshHost, sshPort);
		session.setPassword(sshPassword);
		final Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
	}
	
	
	public void connect() throws JSchException {
		session.connect();
		session.setPortForwardingL(localPort, remoteHost, remotePort);
	}

	public void disconnect() {
		session.disconnect();
	}
}
