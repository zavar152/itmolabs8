package itmo.labs.zavar.server;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ListIterator;

import org.postgresql.PGConnection;

public class ClientUpdater implements Runnable {

	private ArrayList<ClientHandler> clients;
	private Connection connection;
	private PGConnection pgConnection;

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

				org.postgresql.PGNotification notifications[] = pgConnection.getNotifications();
				if (notifications != null) {
					for (int i = 0; i < notifications.length; i++) {
						System.out.println("Got notification: " + notifications[i].getName());

						ListIterator<ClientHandler> iter = clients.listIterator();

						while (iter.hasNext()) {
							ClientHandler handler = iter.next();
							if (handler.isOpen()) {
								handler.addOutput(notifications[i].getParameter());
								handler.writeToClient(ByteBuffer.allocate(0));
							} else {
								iter.remove();
							}
						}
					}

				}

				Thread.sleep(500);
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
		}
	}

}
