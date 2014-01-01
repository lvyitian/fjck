package fjck.ast.command;

import fjck.interpreter.Context;

public class Copy implements Command {

	public final int offset;
	
	public Copy(int offset) {
		this.offset = offset;
	}
	
	@Override
	public void execute(Context ctx) {
		ctx.setCell(
				ctx.getPointer() + offset,
				ctx.getCell(ctx.getPointer() + offset) +
				ctx.getCell(ctx.getPointer()));
	}
	
}
