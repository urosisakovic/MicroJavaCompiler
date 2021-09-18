package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	
	private List<CompilerError> errorList;
	
	private static MJCompiler instance = null;
	
	public static MJCompiler getInstance() {
		if (instance == null) {
			instance = new MJCompiler();
		}
		
		return instance;
	}
	
	private MJCompiler() {
		errorList = new ArrayList<>();
	}

	public void reportError(CompilerError compilerError) {
		errorList.add(compilerError);
	}
	
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
	
	private void checkForLexicalErrors(File sourceCode) {		

		try (BufferedReader reader = new BufferedReader(new FileReader(sourceCode))) {
			Yylex lexer = new Yylex(reader);

			Symbol currToken = null;
			while ((currToken = lexer.next_token()).sym != sym.EOF) {
				if (currToken != null && currToken.value != null)
					log.info(currToken.toString() + " " + currToken.value.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public List<CompilerError> compile(String sourceFilePath, String outputFilePath) {
		errorList = new ArrayList<>();
		
		File sourceCode = new File(sourceFilePath);
		if (!sourceCode.exists()) {
			log.error("Ulazni fajl [" + sourceCode.getAbsolutePath() + "] nije pronadjen!");
			return null;
		}
			
		log.info("Kompajliranje ulaznog fajla " + sourceCode.getAbsolutePath());
		
		checkForLexicalErrors(sourceCode);
		if (errorList.size() > 0) {
			return errorList;
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(sourceCode))) {
			MJParser p = new MJParser(new Yylex(br));
			
	        if (p.isErrorDetected()) {
				log.info("Ulazni fajl ima sintaksnih gresaka!");
				return null;
	        }
	        
	        Symbol s;
	        s = p.parse();
			Program prog = (Program)(s.value); 
			log.info("=====================SINTAKSNO STABLO===================== \n");
			log.info(prog.toString(""));
			
			log.info("=====================SEMANTICKA ANALIZA=====================\n");			
			Tab.init();

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            prog.traverseBottomUp(semanticAnalyzer);
            
            tsdump();
			
			if (!semanticAnalyzer.isErrorDetected()) {
	        	File objFile = new File(outputFilePath);
	        	log.info("Generisanje MJ bajtkoda: " + objFile.getAbsolutePath());

	        	if (objFile.exists()) {
	        		objFile.delete();
	        	}

	        	CodeGenerator codeGenerator = new CodeGenerator(semanticAnalyzer.getOuterScope());
                prog.traverseBottomUp(codeGenerator);
                
                Code.dataSize = semanticAnalyzer.getnVars();
                Code.mainPc = codeGenerator.getMainPc();
                
	        	try {
					Code.write(new FileOutputStream(objFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	        }
	        else {
	        	log.error("Parsiranje nije uspesno zavrseno!");
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.errorList;
	}
	
	public static void tsdump() { 
        Tab.dump(new TableDumpVisitor());
	}
	
}
