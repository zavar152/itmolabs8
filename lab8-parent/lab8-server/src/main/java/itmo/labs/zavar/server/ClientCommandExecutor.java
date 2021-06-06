package itmo.labs.zavar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import itmo.labs.zavar.commands.base.Command;
import itmo.labs.zavar.commands.base.Command.ExecutionType;
import itmo.labs.zavar.commands.base.Environment;
import itmo.labs.zavar.commands.base.net.CommandPackage;
import itmo.labs.zavar.exception.CommandException;

public class ClientCommandExecutor {

	public static ByteBuffer executeCommand(CommandPackage per, Environment clientEnv, String host) throws IOException {
		ByteArrayOutputStream outCom = new ByteArrayOutputStream();
		ByteBuffer outBuffer;
		try {
			Command c = clientEnv.getCommandsMap().get(per.getName());
			if(c.isAuthorizationRequired() && !clientEnv.getUser(host).equals(per.getLogin())) {
				outBuffer = ByteBuffer.wrap("You don't have permissions to execute this command!".getBytes());
			} else {
				if(c.getName().equals("execute_script")) {
					c.execute(ExecutionType.SERVER, clientEnv, ArrayUtils.addAll(per.getArgs(), host), System.in, new PrintStream(outCom));
				} else {
					c.execute(ExecutionType.SERVER, clientEnv, ArrayUtils.addAll(per.getArgs(), host), System.in, new PrintStream(outCom));
				}
				outBuffer = ByteBuffer.wrap(outCom.toByteArray());
			}
		} catch (CommandException e) {
			outBuffer = ByteBuffer.wrap(e.getMessage().getBytes());
		}
		outCom.close();
		return outBuffer;
	}
	
}
