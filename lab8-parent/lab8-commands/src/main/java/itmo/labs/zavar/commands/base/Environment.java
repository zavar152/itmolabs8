package itmo.labs.zavar.commands.base;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import itmo.labs.zavar.db.DataBaseManager;

/**
 * This class contains main information: commands' map, collection, command's
 * history, init time.
 * 
 * @author Zavar
 * @version 1.4
 */

public class Environment {
	private HashMap<String, Command> map;
	private History history;
	private DataBaseManager db;
	private ConcurrentHashMap<String, String> userTable;
	
	/**
	 * Creates new server environment for commands. Collection's creation date will
	 * be equals to file's creation date. If it won't be able to get file's
	 * attributes, collection's creation date will be set to the current.
	 * 
	 * @param file  File with collection.
	 * @param map   Commands' map.
	 * @param stack Main collection.
	 */
	public Environment(ConcurrentHashMap<String, String> userTable, DataBaseManager db, HashMap<String, Command> map) {
		this.map = map;
		history = new History();
		this.userTable = userTable;
		this.db = db;
	}
	/**
	 * Returns DataBaseManager to get connection with database
	 *  
	 * @return DataBaseManager
	 */
	public DataBaseManager getDbManager() {
		return db;
	}

	public void putUser(String host, String user) {
		userTable.put(host, user);
	}
	
	public void removeUser(String host) {
		userTable.remove(host);
	}
	
	public String getUser(String host) {
		return userTable.get(host);
	}
	
	public boolean containsUser(String user) {
		return userTable.contains(user);
	}
	
	public boolean containsHost(String host) {
		return userTable.containsKey(host);
	}
	
	/**
	 * Creates new client environment for commands.
	 * 
	 * @param map Commands' map.
	 */
	public Environment(HashMap<String, Command> map) {
		history = new History();
		this.map = map;
	}

	/**
	 * Returns commands' map.
	 * 
	 * @return {@link HashMap}
	 */
	public HashMap<String, Command> getCommandsMap() {
		return map;
	}
	
	/**
	 * Returns history.
	 * 
	 * @return {@link History}
	 */
	public History getHistory() {
		return history;
	}

	/**
	 * Class uses to contain global history of commands and to contain temp history
	 * of "execute_script" command to prevent recursion.
	 * 
	 * @author Zavar
	 * @version 1.0
	 */
	public class History {
		private Stack<String> globalHistory = new Stack<String>();
		private Stack<String> tempHistory = new Stack<String>();

		/**
		 * Clears temp history.
		 */
		public void clearTempHistory() {
			tempHistory.clear();
		}

		/**
		 * Returns global command history.
		 * 
		 * @return {@link Stack}
		 */
		public Stack<String> getGlobalHistory() {
			return globalHistory;
		}

		/**
		 * Returns temp command history.
		 * 
		 * @return {@link Stack}
		 */
		public Stack<String> getTempHistory() {
			return tempHistory;
		}

		/**
		 * Adds command to global history.
		 * 
		 * @param to Command to add.
		 */
		public void addToGlobal(String to) {
			globalHistory.push(to);
		}

		/**
		 * Adds command to temp history.
		 * 
		 * @param to Command to add.
		 */
		public void addToTemp(String to) {
			tempHistory.push(to);
		}
	}
}
