package fjck.ast.command;

import fjck.interpreter.Context;

public class Add implements Command {
	
	public static final Add INC = new Add(1);
	public static final Add DEC = new Add(-1);
	
	public final int delta;
	
	public Add(int delta) {
		this.delta = delta;
	}

	@Override
	public void execute(Context ctx) {
		int i = ctx.getPointer();
		ctx.setCell(i, ctx.getCell(i) + delta);
	}
}
