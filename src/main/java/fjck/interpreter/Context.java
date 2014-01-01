package fjck.interpreter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class Context {
	private final Reader reader;
	private final Writer writer;
	
	private int pointer;
	private int[] cells;
	
	public Context(int arraySize, Reader reader, Writer writer) {
		this.reader = reader;
		this.writer = writer;
		pointer = 0;
		cells = new int[arraySize];
	}
	
	public int getPointer() {
		return pointer;
	}
	
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}
	
	public int getSize() {
		return cells.length;
	}
	
	public int getCell(int index) {
		return cells[index];
	}
	
	public void setCell(int index, int value) {
		cells[index] = value;
	}
	
	public int readChar() {
		try {
			return reader.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void writeChar(int c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void flushWrite() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
