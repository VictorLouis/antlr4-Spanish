
package unal.lenguajes.parcialfinal;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {

	private static final String EXTENSION = "pfl";

	public static void main(String[] args) throws IOException {
		String program = args.length > 1 ? args[1] : "test/test11." + EXTENSION;

		System.out.println("Interpreting file " + program);

		LynxLexer lexer = new LynxLexer(new ANTLRFileStream(program));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		LynxParser parser = new LynxParser(tokens);

		ParseTree tree = parser.programa();

		LynxCustomVisitor visitor = new LynxCustomVisitor();
		visitor.visit(tree);

		System.out.println("Interpretation finished");

	}

}
