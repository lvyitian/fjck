package fjck.ast.command;

import fjck.interpreter.Context;

public interface Command {
	public void execute(Context ctx);
}
