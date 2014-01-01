package fjck.compiler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import fjck.Program;
import fjck.ast.command.Add;
import fjck.ast.command.Block;
import fjck.ast.command.Command;
import fjck.ast.command.Loop;
import fjck.ast.command.Move;
import fjck.ast.command.Read;
import fjck.ast.command.Write;

public class Compiler implements Opcodes {
	
	/**
	 * Used to ensure every compiled program has a unique class name.
	 */
	private static final AtomicInteger genCounter = new AtomicInteger();
	
	/**
	 * Some common local variable indices
	 */
	private static final int LOCAL_THIS = 0;
	private static final int LOCAL_INPUT = 1;
	private static final int LOCAL_OUTPUT = 2;
	private static final int LOCAL_ARRAY = 3;
	private static final int LOCAL_POINTER = 4;
	
	/**
	 * Build the constructor for the new Program implementation.
	 * @param cw
	 * @param className
	 */
	private static void buildConstructor(ClassWriter cw, String className) {
		
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(1, l0);
		mv.visitVarInsn(ALOAD, LOCAL_THIS);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "Lfjck/" + className + ";", null, l0, l1, LOCAL_THIS);
		mv.visitMaxs(1, 1);
		
		mv.visitEnd();
	}
	
	/**
	 * Add some common code at the start of the execute method. Initialize the
	 * array and array pointer.
	 * 
	 * @param mv
	 * @return
	 */
	private static Label buildExecuteCommon(MethodVisitor mv) {
        
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(2, l0);
        
        // init array
        mv.visitIntInsn(SIPUSH, 30000);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitVarInsn(ASTORE, LOCAL_ARRAY);
        
        // init pointer
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, LOCAL_POINTER);
        
        return l0;
	}
	
	/**
	 * Generate code for an Add command.
	 * @param mv
	 * @param add
	 */
	private static void buildAdd(MethodVisitor mv, Add add) {
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(DUP2);
		mv.visitInsn(IALOAD);
		mv.visitIntInsn(BIPUSH, add.delta);
		mv.visitInsn(IADD);
		mv.visitInsn(IASTORE);
	}
	
	/**
	 * Generate code for a Move command.
	 * @param mv
	 * @param move
	 */
	private static void buildMove(MethodVisitor mv, Move move) {
		mv.visitIincInsn(LOCAL_POINTER, move.delta);
	}
	
	/**
	 * Generate code for a Read command.
	 * @param mv
	 * @param read
	 */
	private static void buildRead(MethodVisitor mv, Read read) {
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
        mv.visitVarInsn(ILOAD, LOCAL_POINTER);
        mv.visitVarInsn(ALOAD, LOCAL_INPUT);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "read", "()I");
        mv.visitInsn(IASTORE);
	}
	
	/**
	 * Generate code for a Write command.
	 * @param mv
	 * @param write
	 */
	private static void buildWrite(MethodVisitor mv, Write write) {
		mv.visitVarInsn(ALOAD, LOCAL_OUTPUT);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(IALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Writer", "write", "(I)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Writer", "flush", "()V");
	}
	
	/**
	 * Generate code for all commands within a Block.
	 * @param mv
	 * @param block
	 */
	private static void buildBlock(MethodVisitor mv, Block block) {
		for (Command c : block.getCommands()) {
			if (Loop.class.isInstance(c)) {
				buildLoop(mv, Loop.class.cast(c));
			} else if (Block.class.isInstance(c)) {
				buildBlock(mv, Block.class.cast(c));
			} else if (Add.class.isInstance(c)) {
				buildAdd(mv, Add.class.cast(c));
			} else if (Move.class.isInstance(c)) {
				buildMove(mv, Move.class.cast(c));
			} else if (Read.class.isInstance(c)) {
				buildRead(mv, Read.class.cast(c));
			} else if (Write.class.isInstance(c)) {
				buildWrite(mv, Write.class.cast(c));
			}
		}
	}
	
	/**
	 * Generate the control flow of a Loop command around the commands
	 * contained within the Block (Loop being a subclass of Block).
	 * @param mv
	 * @param loop
	 */
	private static void buildLoop(MethodVisitor mv, Loop loop) {
		
		Label lblCheck = new Label();
		Label lblBlock = new Label();
		
		/**
		 * Jump ahead to the conditional jump
		 */
		mv.visitJumpInsn(GOTO, lblCheck);
		
		/**
		 * Label the code block so we can jump back to it.
		 */
		mv.visitLabel(lblBlock);
		mv.visitFrame(F_SAME,
				5, new Object[] { LOCAL_THIS, LOCAL_INPUT, LOCAL_OUTPUT, LOCAL_ARRAY, LOCAL_POINTER },
				0, new Object[] {});
		
		buildBlock(mv, loop);
		
		/**
		 * Conditional jump, go to the start of the block if the value
		 * of the array at our pointer is nonzero.
		 */
		mv.visitLabel(lblCheck);
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(IALOAD);
		
		mv.visitJumpInsn(IFNE, lblBlock);
		mv.visitFrame(F_SAME,
				5, new Object[] { LOCAL_THIS, LOCAL_INPUT, LOCAL_OUTPUT, LOCAL_ARRAY, LOCAL_POINTER },
				0, new Object[] {});
		
		
	}
	
	/**
	 * Generate code to declare local variables.
	 * @param mv
	 * @param l0
	 * @param className
	 */
	private static void buildLocals(MethodVisitor mv, Label l0, String className) {
		Label lblLocals = new Label();
        mv.visitLabel(lblLocals);
        mv.visitLocalVariable("this", "Lfjck/" + className + ";", null, l0, lblLocals, LOCAL_THIS);
        mv.visitLocalVariable("input", "Ljava/io/Reader;", null, l0, lblLocals, LOCAL_INPUT);
        mv.visitLocalVariable("output", "Ljava/io/Writer;", null, l0, lblLocals, LOCAL_OUTPUT);
        mv.visitLocalVariable("array", "[I", null, l0, lblLocals, LOCAL_ARRAY);
        mv.visitLocalVariable("pointer", "I", null, l0, lblLocals, LOCAL_POINTER);
	}
	
	/**
	 * Build the execute method for a new Program implementation.
	 * @param cw
	 * @param block
	 * @param className
	 */
	private static void buildExecute(ClassWriter cw, Block block, String className) {
		MethodVisitor mv = cw.visitMethod(
				ACC_PUBLIC,
				"execute", "(Ljava/io/Reader;Ljava/io/Writer;)V",
				null, null);
        mv.visitCode();
        
		Label l0 = buildExecuteCommon(mv);
		
		buildBlock(mv, block);
		
		mv.visitInsn(RETURN);
		
		buildLocals(mv, l0, className);
		
        mv.visitMaxs(4, 5);
        mv.visitEnd();
	}
	
	/**
	 * Take a Block and className, and turn it into an array of corresponding bytecode.
	 * @param block
	 * @param className
	 * @return
	 * @throws IOException
	 */
	private static byte[] compileBytecode(Block block, String className) throws IOException {
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_6,
				ACC_PUBLIC + ACC_SUPER,
				"fjck/" + className,
				null,
				"java/lang/Object",
				new String[] { "fjck/Program" });
		
		buildConstructor(cw, className);
		buildExecute(cw, block, className);
		cw.visitEnd();
		
		return cw.toByteArray();
	}
	
	/**
	 * Simple helper/wrapper around a ClassLoader.
	 * @author pdehaan
	 *
	 */
	private static class DynamicClassLoader extends ClassLoader {
		public DynamicClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		public Class<?> define(String className, byte[] bytecode) {
			return super.defineClass(className, bytecode, 0, bytecode.length);
		}
	}
	
	/**
	 * Take a Block, compile it, and return a new
	 * Program instance from the resulting Class.
	 * @param block
	 * @return
	 */
	public static Program compileProgram(Block block) {
		
		/**
		 * Generate a unique class name.
		 */
		int gen = genCounter.getAndIncrement();
		String className = "Program_gen_" + gen;
		
		/**
		 * Compile to bytecode or fail spectacularly.
		 */
		byte[] bytecode;
		try {
			bytecode = compileBytecode(block, className);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		/**
		 * Ready a DynamicClassLoader with to create a new Class.
		 */
		DynamicClassLoader loader = new DynamicClassLoader(
				Thread.currentThread().getContextClassLoader());
		Class<?> klass = loader.define("fjck." + className, bytecode);
		
		/**
		 * Get an instance of the class or die trying.
		 */
		try {
			return (Program) klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
