package itmo.labs.zavar.commands.base;

import java.io.Serializable;

public class CommandPackage implements Serializable {
	
	private static final long serialVersionUID = -7071028630270434499L;
	
	private String name;
	private Object[] args;
	private String login, password;
	
	public CommandPackage(String name, Object[] args, String login, String password) {
		this.name = name;
		this.args = args;
		this.login = login;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public Object[] getArgs() {
		return args;
	}
}
