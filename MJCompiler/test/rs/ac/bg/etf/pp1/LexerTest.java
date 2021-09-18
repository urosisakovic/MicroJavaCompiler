package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.util.Log4JUtils;

public class LexerTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws IOException {
		Logger log = Logger.getLogger("info");
		
		Reader reader = null;
		try {
			
			File sourceCode = new File("test/program.mj");	
			log.info("Testing lexer on file " + sourceCode.getAbsolutePath());
			
			reader = new BufferedReader(new FileReader(sourceCode));
			
			Yylex lexer = new Yylex(reader);

			Symbol currToken = null;
			while ((currToken = lexer.next_token()).sym != sym.EOF) {
				if (currToken != null && currToken.value != null)
					log.info(currToken.toString() + " " + currToken.value.toString());
			}
		}
		catch (Exception e) {
		}
		finally {
			if (reader != null) try { reader.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}
	}
	
}
