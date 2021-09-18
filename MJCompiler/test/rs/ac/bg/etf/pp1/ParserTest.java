package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;

public class ParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger("info");
		
		Reader reader = null;
		try {
			File sourceCode = new File("test/program.mj");
			log.info("Testing parser on file " + sourceCode.getAbsolutePath());
			
			reader = new BufferedReader(new FileReader(sourceCode));

			Yylex lexer = new Yylex(reader);			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();
	        Program prog = (Program)(s.value);

			log.info(prog.toString(""));
		} 
		finally {
			if (reader != null) try { reader.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}
	}
		
}
