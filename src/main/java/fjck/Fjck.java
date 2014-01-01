package fjck;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.objectweb.asm.Opcodes;

import fjck.ast.Parser;
import fjck.ast.command.Block;

public class Fjck implements Opcodes {
	
	public static void main(String[] args) throws Exception {
		InputStream source;
		InputStream in;
		OutputStream out;
		
		if (args.length > 0) {
			// first argument is file to run
			source = new FileInputStream(new File(args[0]));
		} else {
			// load a default program
			source = ClassLoader.getSystemResourceAsStream("hello.bf.txt");
		}
		
		if (args.length > 1) {
			// second argument is file to use as input to program
			in = new FileInputStream(new File(args[1]));
		} else {
			in = System.in;
		}
		
		out = System.out;

		Block block = Parser.parse(new InputStreamReader(source));
		source.close();
		
		Program program = fjck.compiler.Compiler.compileProgram(block);
		
		program.execute(
				new InputStreamReader(in),
				new OutputStreamWriter(out));
	}
}
