package fjck.ast.command;

import fjck.interpreter.Context;

public class Write implements Command {

	public static final Write INSTANCE = new Write();
	
	@Override
	public void execute(Context ctx) {
		ctx.writeChar(ctx.getCell(ctx.getPointer()));
	}

}
