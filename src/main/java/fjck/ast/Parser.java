package fjck.ast;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

import fjck.ast.command.Add;
import fjck.ast.command.Block;
import fjck.ast.command.Loop;
import fjck.ast.command.Move;
import fjck.ast.command.Read;
import fjck.ast.command.Write;

public class Parser {

	public static Block parse(Reader reader) throws IOException {
		Block top = new Block();
		Deque<Block> blockStack = new ArrayDeque<>();
		blockStack.addFirst(top);
		
		int r;
		while (-1 != (r = reader.read())) {
			switch (r) {
			case '+':
				blockStack.peekFirst().addCommand(Add.INC);
				break;
			case '-':
				blockStack.peekFirst().addCommand(Add.DEC);
				break;
			case '>':
				blockStack.peekFirst().addCommand(Move.FORWARD);
				break;
			case '<':
				blockStack.peekFirst().addCommand(Move.BACKWARD);
				break;
			case '.':
				blockStack.peekFirst().addCommand(Write.INSTANCE);
				break;
			case ',':
				blockStack.peekFirst().addCommand(Read.INSTANCE);
				break;
			case '[':
				Loop loop = new Loop();
				blockStack.peekFirst().addCommand(loop);
				blockStack.addFirst(loop);
				break;
			case ']':
				if (blockStack.isEmpty()) {
					throw new IOException("unmatched ']'");
				}
				blockStack.pop();
			default:
			}
		}
		
		return top;
	}
}
