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
	
	private static final AtomicInteger genCounter = new AtomicInteger();
	
	private static final int LOCAL_THIS = 0;
	private static final int LOCAL_INPUT = 1;
	private static final int LOCAL_OUTPUT = 2;
	private static final int LOCAL_ARRAY = 3;
	private static final int LOCAL_POINTER = 4;
	
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
	
	private static Label buildExecuteCommon(MethodVisitor mv) {
        
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(2, l0);
        
        // create array
        mv.visitIntInsn(SIPUSH, 30000);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitVarInsn(ASTORE, LOCAL_ARRAY);
        
        // create pointer
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, LOCAL_POINTER);
        
        return l0;
	}
	
	private static void buildAdd(MethodVisitor mv, Add add) {
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(DUP2);
		
		mv.visitInsn(IALOAD);
		mv.visitIntInsn(BIPUSH, add.delta);
		mv.visitInsn(IADD);
		mv.visitInsn(IASTORE);
	}
	
	private static void buildMove(MethodVisitor mv, Move move) {
		mv.visitIincInsn(LOCAL_POINTER, move.delta);
	}
	
	private static void buildRead(MethodVisitor mv, Read read) {
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
        mv.visitVarInsn(ILOAD, LOCAL_POINTER);
        mv.visitVarInsn(ALOAD, LOCAL_INPUT);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Reader", "read", "()I");
        mv.visitInsn(IASTORE);
	}
	
	private static void buildWrite(MethodVisitor mv, Write write) {
		mv.visitVarInsn(ALOAD, LOCAL_OUTPUT);
		mv.visitInsn(DUP);
		
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(IALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Writer", "write", "(I)V");
		
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Writer", "flush", "()V");
	}
	
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
	
	private static void buildLoop(MethodVisitor mv, Loop loop) {
		
		Label lblCheck = new Label();
		Label lblBlock = new Label();
		
		mv.visitJumpInsn(GOTO, lblCheck);
		
		mv.visitLabel(lblBlock);
		mv.visitFrame(F_SAME,
				5, new Object[] { LOCAL_THIS, LOCAL_INPUT, LOCAL_OUTPUT, LOCAL_ARRAY, LOCAL_POINTER },
				0, new Object[] {});
		
		buildBlock(mv, loop);
		
		mv.visitLabel(lblCheck);
		mv.visitVarInsn(ALOAD, LOCAL_ARRAY);
		mv.visitVarInsn(ILOAD, LOCAL_POINTER);
		mv.visitInsn(IALOAD);
		
		mv.visitJumpInsn(IFNE, lblBlock);
		mv.visitFrame(F_SAME,
				5, new Object[] { LOCAL_THIS, LOCAL_INPUT, LOCAL_OUTPUT, LOCAL_ARRAY, LOCAL_POINTER },
				0, new Object[] {});
		
		
	}
	
	private static void buildLocals(MethodVisitor mv, Label l0, String className) {
		Label lblLocals = new Label();
        mv.visitLabel(lblLocals);
        mv.visitLocalVariable("this", "Lfjck/" + className + ";", null, l0, lblLocals, LOCAL_THIS);
        mv.visitLocalVariable("input", "Ljava/io/Reader;", null, l0, lblLocals, LOCAL_INPUT);
        mv.visitLocalVariable("output", "Ljava/io/Writer;", null, l0, lblLocals, LOCAL_OUTPUT);
        mv.visitLocalVariable("array", "[I", null, l0, lblLocals, LOCAL_ARRAY);
        mv.visitLocalVariable("pointer", "I", null, l0, lblLocals, LOCAL_POINTER);
	}
	
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
	
	private static class DynamicClassLoader extends ClassLoader {
		public DynamicClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		public Class<?> define(String className, byte[] bytecode) {
			return super.defineClass(className, bytecode, 0, bytecode.length);
		}
	}
	
	public static Program compileProgram(Block block) {
		
		int gen = genCounter.getAndIncrement();
		String className = "Program_gen_" + gen;
		
		byte[] bytecode;
		try {
			bytecode = compileBytecode(block, className);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		DynamicClassLoader loader = new DynamicClassLoader(
				Thread.currentThread().getContextClassLoader());
		
		Class<?> klass = loader.define("fjck." + className, bytecode);
		
		try {
			return (Program) klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}
