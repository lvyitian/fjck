package fjck;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface Program {
	void execute(Reader input, Writer output) throws IOException;
}
