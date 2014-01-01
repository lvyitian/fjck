package fjck.interpreter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import fjck.Program;
import fjck.ast.command.Block;

public class InterpretedProgram implements Program {

	private final Block block;
	
	public InterpretedProgram(Block block) {
		this.block = block;
	}
	
	@Override
	public void execute(Reader input, Writer output) throws IOException {
		Context ctx = new Context(30000, input, output);
		block.execute(ctx);
		ctx.flushWrite();
	}

}
