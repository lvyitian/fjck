package fjck.ast.command;

import fjck.interpreter.Context;

public class Loop extends Block {

	@Override
	public void execute(Context ctx) {
		while (0 != ctx.getCell(ctx.getPointer())) {
			super.execute(ctx);
		}
	}

}
