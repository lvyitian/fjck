package fjck.ast.command;

import fjck.interpreter.Context;

public class CopyMultiply implements Command {

	public final int offset;
	public final int factor;
	
	public CopyMultiply(int offset, int factor) {
		this.offset = offset;
		this.factor = factor;
	}
	
	@Override
	public void execute(Context ctx) {
		int a = ctx.getCell(ctx.getPointer());
		int b = a * factor;
		int c = ctx.getCell(ctx.getPointer() + offset) + b;
		ctx.setCell(ctx.getPointer() + offset, c);
	}

}
