package fjck.optimizer;

import java.util.Deque;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import fjck.ast.command.Add;
import fjck.ast.command.Block;
import fjck.ast.command.Command;
import fjck.ast.command.Copy;
import fjck.ast.command.CopyMultiply;
import fjck.ast.command.Loop;
import fjck.ast.command.Move;
import fjck.ast.command.Zero;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

public class LinearOptimizer {

	private static boolean isAdd(Command c) {
		return Add.class.isInstance(c);
	}
	
	private static Add asAdd(Command c) {
		return Add.class.cast(c);
	}
	
	private static boolean isMove(Command c) {
		return Move.class.isInstance(c);
	}
	
	private static Move asMove(Command c) {
		return Move.class.cast(c);
	}
	
	private static boolean isLoop(Command c) {
		return Loop.class.isInstance(c);
	}
	
	private static boolean containsNotAddOrMove(Block block) {
		for (Command c : block.getCommands()) {
			if (!(isAdd(c) || isMove(c))) {
				return true;
			}
		}
		return false;
	}
	
	private static int totalMove(Block block) {
		int n = 0;
		for (Command c : block.getCommands()) {
			if (Move.class.isInstance(c)) {
				n += Move.class.cast(c).delta;
			}
		}
		return n;
	}
	
	public static Block optimize(Block input) {
		Deque<Block> blocks = Queues.newArrayDeque();
		blocks.add(input);
		
		while (!blocks.isEmpty()) {
			Block block = blocks.pollFirst();
			if (containsNotAddOrMove(block)) {
				/**
				 * We can't optimize this block itself, by child blocks
				 * may be optimizable we can still join consecutive Add/Move commands though
				 */
				
				Deque<Command> newCommands = Queues.newArrayDeque();
				for (Command c : block.getCommands()) {
					if (isLoop(c)) {
						blocks.addFirst(Block.class.cast(c));
					}
					
					if (isAdd(newCommands.peekLast()) && isAdd(c)) {
						newCommands.addLast(new Add(
								asAdd(newCommands.removeLast()).delta +
								asAdd(c).delta));
					} else if (isMove(newCommands.peekLast()) && isMove(c)) {
						newCommands.addLast(new Move(
								asMove(newCommands.removeLast()).delta +
								asMove(c).delta));
					} else {
						newCommands.addLast(c);
					}
				}
				block.setCommands(Lists.newLinkedList(newCommands));
			} else if (isLoop(block) && 0 == totalMove(block)){
				/**
				 * This loop block only contains moves and adds, and has balanced moves,
				 * so we can break it down to copy/multiplies if the start index
				 * is being shift down 1 per iteration
				 */
				TIntIntMap offsetAdds = new TIntIntHashMap();
				int offset = 0;
				for (Command c : block.getCommands()) {
					if (isMove(c)) {
						offset += asMove(c).delta;
					} else if (isAdd(c)) {
						offsetAdds.adjustOrPutValue(offset, asAdd(c).delta, asAdd(c).delta);
					}
				}
				
				if (offsetAdds.get(0) != -1) {
					System.err.println(offsetAdds.get(0));
					continue;
				}
				
				final List<Command> newCommands = Lists.newLinkedList();
				offsetAdds.forEachEntry(new TIntIntProcedure() {
					@Override
					public boolean execute(int offset, int delta) {
						if (offset == 0) {
							return true;
						} else if (delta == 1) {
							newCommands.add(new Copy(offset));
						} else if (delta != 0) {
							newCommands.add(new CopyMultiply(offset, delta));
						}
						return true;
					}
				});
				newCommands.add(new Zero());
				
				block.setCommands(newCommands);
			}
		}
		
		return input;
	}
}
