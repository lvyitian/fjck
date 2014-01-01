package fjck.ast.command;

import fjck.interpreter.Context;

public class Move implements Command {
	
	public static final Move FORWARD = new Move(1);
	public static final Move BACKWARD = new Move(-1);
	
	public final int delta;
	
	public Move(int delta) {
		this.delta = delta;
	}

	@Override
	public void execute(Context ctx) {
		ctx.setPointer(ctx.getPointer() + delta);
	}
}
