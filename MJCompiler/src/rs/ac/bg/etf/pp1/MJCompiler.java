package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;
import rs.etf.pp1.mj.runtime.Code;

public class MJCompiler implements Compiler {

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
	
	@Override
	public List<CompilerError> compile(String sourceFilePath, String outputFilePath) {
		File sourceCode = new File(sourceFilePath);
		if (!sourceCode.exists()) {
			log.error("Ulazni fajl [" + sourceCode.getAbsolutePath() + "] nije pronadjen!");
			return null;
		}
			
		log.info("Kompajliranje ulaznog fajla " + sourceCode.getAbsolutePath());
		
		try (BufferedReader br = new BufferedReader(new FileReader(sourceCode))) {
			Yylex lexer = new Yylex(br);			
			MJParser p = new MJParser(lexer);
			
	        if (p.isErrorDetected()) {
				log.info("Ulazni fajl ima sintaksnih gresaka!");
				return null;
	        }
	        
	        Symbol s;
	        s = p.parse();
			Program prog = (Program)(s.value); 

			log.info("\n\n================SINTAKSNO STABLO====================\n\n");
			log.info(prog.toString(""));
			
			log.info("\n\n================SEMANTICKA OBRADA====================\n\n");			
			Tab.init(); // pocetni opseg

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            prog.traverseBottomUp(semanticAnalyzer);
            
            tsdump();
			log.info("===================================");
			
			if (!semanticAnalyzer.isErrorDetected()) {
	        	File objFile = new File(outputFilePath);
	        	log.info("Generisanje MJ bajtkoda: " + objFile.getAbsolutePath());
	        	if (objFile.exists())
	        		objFile.delete();

	        	CodeGenerator codeGenerator = new CodeGenerator(semanticAnalyzer.getOuterScope());
                prog.traverseBottomUp(codeGenerator);
                
                Code.dataSize = semanticAnalyzer.getnVars();
                Code.mainPc = codeGenerator.getMainPc();
                
	        	try {
					Code.write(new FileOutputStream(objFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	log.info("Parsiranje uspesno zavrseno!");
	        }
	        else {
	        	log.error("Parsiranje nije uspesno zavrseno!");
	        }
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			log.error("Nema dovoljno argumenata! ( Ocekivani ulaz: <ulaz>.mj <izlaz>.obj )");
			return;
		}
		
		MJCompiler mjCompiler = new MJCompiler();
		List<CompilerError> compilerErrorList = mjCompiler.compile(args[0], args[1]);
		
		if (compilerErrorList == null) {
			
		}
		else if (compilerErrorList.size() == 0) {
			
		}
		else {
			for (var compilerError: compilerErrorList) {
				log.error(compilerError.toString());
			}
		}
		
		
	}
	
	public static void tsdump() {
		SymbolTableVisitor stv = new MySymbolTableVisitor();
        Tab.dump(stv);
	}
	
}
