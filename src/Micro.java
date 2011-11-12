import java.io.*;
import java.util.*;
import org.antlr.runtime.*;

public class Micro {
    public static void main (String[] args) throws Exception {
	// Step1 code
        /*CharStream input = new ANTLRFileStream(args[0]);
        MicroParser lexer = new MicroParser(input);
        Token token;
        String tokenType = "";
        int i = 0;

        while ((token = lexer.nextToken()).getType() != MicroParser.EOF) {
            switch (token.getType()) {
                case MicroParser.IDENTIFIER:    tokenType = "IDENTIFIER";       break;
                case MicroParser.INTLITERAL:    tokenType = "INTLITERAL";       break;
                case MicroParser.FLOATLITERAL:  tokenType = "FLOATLITERAL";     break;
                case MicroParser.STRINGLITERAL: tokenType = "STRINGLITERAL";    break;
                case MicroParser.COMMENT:       tokenType = "COMMENT";          break;
                case MicroParser.KEYWORD:       tokenType = "KEYWORD";          break;
                case MicroParser.OPERATOR:      tokenType = "OPERATOR";         break;
                default:                        tokenType = "UNDEF?";
            }
            System.out.println("Token Type: " + tokenType);
            System.out.println("Value: " + token.getText());
        }*/

	// Step2 code
	//try {
	MicroParserLexer lex = new MicroParserLexer(new ANTLRFileStream(args[0]));
	CommonTokenStream tokens = new CommonTokenStream(lex);
	MicroParserParser parser = new MicroParserParser(tokens);

	try {
		parser.program();
		/*parser.program();
		if (parser.getErrorCount() > 0) {
			System.out.println("Not accepted");
		}
		else {
			System.out.println("Accepted");
		}*/
	} catch (RecognitionException t) {
		System.out.println("Line " + t.line + " char " + t.charPositionInLine);
		//System.out.println("Not Accepted");
	}

    }
}
