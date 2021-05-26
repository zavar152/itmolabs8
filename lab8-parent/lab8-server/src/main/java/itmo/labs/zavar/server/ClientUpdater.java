package itmo.labs.zavar.server;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import itmo.labs.zavar.db.DbUtils;

public class ClientUpdater implements Runnable {

	private ArrayList<ClientHandler> clients;
	private Connection connection;
	private PGConnection pgConnection;
	private Logger logger = LogManager.getLogger(ClientUpdater.class.getName());

	public ClientUpdater(ArrayList<ClientHandler> clients, Connection connection) throws SQLException {
		this.clients = clients;
		this.connection = connection;
		this.pgConnection = connection.unwrap(PGConnection.class);
		Statement stmt = connection.createStatement();
		stmt.execute("LISTEN checker");
		stmt.close();
	}

	@Override
	public void run() {
		try {
			while (true) {

				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT 1");
				rs.close();
				stmt.close();

				PGNotification notifications[] = pgConnection.getNotifications();
				if (notifications != null) {
					for (int i = 0; i < notifications.length; i++) {
						logger.info("Got notification: " + notifications[i].getName());

						String res = "";
						String[] args = notifications[i].getParameter().split(";");
						
						if(args[0].equals("INSERT")) {
							Statement stmt1 = connection.createStatement();
							ResultSet rs1 = stmt1.executeQuery(DbUtils.getById(Integer.parseInt(args[1])));
							res = "INSERT;";
							while(rs1.next()) {
								for(int j = 1; j <= 19; j++) {
									res = res + rs1.getString(j) + ";";	
								}
							}
							rs1.close();
							stmt1.close();
						} else if(args[0].equals("DELETE")) {
							res = args[0] + ";" + args[1];
						} else if(args[0].equals("UPDATE")) {
							Statement stmt1 = connection.createStatement();
							ResultSet rs1 = stmt1.executeQuery(DbUtils.getById(Integer.parseInt(args[1])));
							res = "UPDATE;";
							while(rs1.next()) {
								for(int j = 1; j <= 19; j++) {
									res = res + rs1.getString(j) + ";";	
								}
							}
							rs1.close();
							stmt1.close();
						} else if(args[0].equals("TRUNCATE")) {
							res = args[0];
						}
						
						
						ListIterator<ClientHandler> iter = clients.listIterator();

						while (iter.hasNext()) {
							ClientHandler handler = iter.next();
							if (handler.isOpen()) {
								handler.addOutput(res);
								handler.writeToClient(ByteBuffer.allocate(0));
							} else {
								iter.remove();
							}
						}
					}

				}

				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			//e.printStackTrace();
		}
	}

}
