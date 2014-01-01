package fjck.ast.command;

import java.util.List;

import com.google.common.collect.Lists;

import fjck.interpreter.Context;

public class Block implements Command {

	private List<Command> commands;
	
	public Block() {
		commands = Lists.newLinkedList();
	}
	
	public void addCommand(Command command) {
		commands.add(command);
	}
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}

	@Override
	public void execute(Context ctx) {
		for (Command c : commands) {
			c.execute(ctx);
		}
	}
}
