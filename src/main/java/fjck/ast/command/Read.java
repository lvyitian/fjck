package fjck.ast.command;

import fjck.interpreter.Context;

public class Read implements Command {

	public static final Read INSTANCE = new Read();
	
	@Override
	public void execute(Context ctx) {
		ctx.setCell(ctx.getPointer(), ctx.readChar());
	}

}
