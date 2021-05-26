package itmo.labs.zavar.commands.base.net;

import java.io.Serializable;

public class CommandAnswer implements Serializable {

	private static final long serialVersionUID = -5474045045029448754L;
	
	private Object[] data;
	private String login;
	private String answer;

	public CommandAnswer(String answer, String login, Object[] data) {
		this.answer = answer;
		this.login = login;
		this.data = data;
	}

	public String getLogin() {
		return login;
	}

	public String getAnswer() {
		return answer;
	}

	public Object[] getData() {
		return data;
	}

}
