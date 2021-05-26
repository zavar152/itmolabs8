package itmo.labs.zavar.db;

import java.io.Console;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSchException;

public class DataBaseManager {

	private SshTunnel tunnel = null;
	private static String user, password, baseName, host;
	private static int localPort;
	private static Logger logger = LogManager.getLogger(DataBaseManager.class.getName());
	@SuppressWarnings("unused")
	private static BasicDataSource ds = new BasicDataSource();

	@SuppressWarnings("resource")
	public DataBaseManager(String ssh, String user, String password, String sshHost, String baseName, int sshPort,
			String remoteHost, int localPort, int remotePort) {

		int attempts = 0;
		Scanner scanner = new Scanner(System.in);
		Console console = System.console();
		System.out.println("Enter your password: ");
		while (true) {
			try {
				if (console != null) {
					password = new String(console.readPassword());
				} else {
					password = scanner.nextLine();
				}
				DataBaseManager.user = user;
				DataBaseManager.localPort = localPort;
				DataBaseManager.password = password;
				DataBaseManager.baseName = baseName;
				DataBaseManager.host = "localhost";

				if (ssh.equals("tun")) {
					try {
						tunnel = new SshTunnel(user, password, sshHost, sshPort, remoteHost, localPort, remotePort);
						tunnel.connect();
						logger.info("Created ssh tunnel successfully");
						break;
					} catch (JSchException e) {
						attempts++;
						logger.error(e.getMessage());
						if (attempts == 3) {
							logger.error("You tried to login 3 times, maybe the database is unavailable or your data is incorrect");
							System.exit(0);
						}
					}
				}
				else if(ssh.equals("hel")) {
					DataBaseManager.host = remoteHost;
					DataBaseManager.localPort = 5432;
					break;
				} else {
					DataBaseManager.host = "localhost";
					DataBaseManager.localPort = 2220;
					break;
				}
			} catch (Exception e) {
				if (!scanner.hasNextLine()) {
					logger.info("Inputing is closed!");
					System.exit(0);
				} else {
					e.printStackTrace();
					logger.error("Unexcepted error!");
				}
			}
		}
	}

	public synchronized Connection getConnection() throws SQLException {
		return DBCPDataSource.getConnection();
	}

	public void stop() {

		DBCPDataSource.close();

		if (tunnel != null) {
			tunnel.disconnect();
		}
	}

	private static class DBCPDataSource {

		private static BasicDataSource ds = new BasicDataSource();

		static {
	        ds.setUrl("jdbc:postgresql://" + host + ":" + localPort + "/" + baseName);
	        ds.setUsername(user);
	        ds.setPassword(password);
	        ds.setMinIdle(4);
	        ds.setMaxIdle(21);
	        ds.setMaxOpenPreparedStatements(100);
		}
		
		public static Connection getConnection() throws SQLException {
			return ds.getConnection();
		}

		public static void close() {
			try {
				ds.close();
			} catch (SQLException e) {
				logger.error("Failed to close connection pool");
			}
		}

		private DBCPDataSource() {
		}
	}
}
