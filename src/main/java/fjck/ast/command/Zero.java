package fjck.ast.command;

import fjck.interpreter.Context;

public class Zero implements Command {

	@Override
	public void execute(Context ctx) {
		ctx.setCell(ctx.getPointer(), 0);
	}
	
}
