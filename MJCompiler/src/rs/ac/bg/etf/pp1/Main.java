package rs.ac.bg.etf.pp1;

import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Main {
	
	private static Logger log = Logger.getLogger("info");
	private static Logger logError = Logger.getLogger("error");
	private static FileAppender fileAppender, fileAppenderError;
	
	static {
		fileAppender = new FileAppender();
		fileAppender.setLayout(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN));
		fileAppender.setFile("test/info.out");
		fileAppender.activateOptions();
		log.addAppender(fileAppender);
		
		fileAppenderError = new FileAppender();
		fileAppenderError.setLayout(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN));
		fileAppenderError.setFile("test/error.err");
		fileAppenderError.activateOptions();
		logError.addAppender(fileAppenderError);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			log.error("Nema dovoljno argumenata! ( Ocekivani ulaz: <ulaz>.mj <izlaz>.obj )");
			return;
		}

		MJCompiler mjCompiler = MJCompiler.getInstance();
		
		List<CompilerError> compilerErrorList = mjCompiler.compile(args[0], args[1]);
		
		if (compilerErrorList == null) {
			
		}
		else if (compilerErrorList.size() == 0) {
        	log.info("Prevodjenje uspesno zavrseno!");
		}
		else {
			log.info("Broj gresaka: " + compilerErrorList.size());
			for (var compilerError: compilerErrorList) {
				log.error(compilerError.toString());
			}
		}	
	}
	
}
