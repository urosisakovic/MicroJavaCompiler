package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	public static Struct boolType = Tab.insert(Obj.Type, "bool", new Struct(5)).getType();
	
	int nVars;
	boolean errorDetected = false;
	boolean mainMethodDefined = false;
	int doWhileDepth = 0;
	Obj outerScopeObj = null;
	
	ArrayList<Variable> declarationVariables = new ArrayList<Variable>();
	ArrayList<Method> methods = new ArrayList<Method>();
	ArrayList<Struct> currentMethodParams = new ArrayList<Struct>();
	Obj currentMethod = null;
	Struct assignmentRight = null;
	 
	Logger log = Logger.getLogger(getClass());
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
		
		MJCompiler.getInstance().reportError(new CompilerError(
			line,
        	msg.toString(),
        	CompilerError.CompilerErrorType.SEMANTIC_ERROR));
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	public boolean isErrorDetected() {
		return errorDetected;
	}
	
	public int getnVars() {
		return nVars;
	}
	
	public Obj getOuterScope() {
		return outerScopeObj;
	}
	
	public boolean methodContains(String paramName) {
		for(var localSymbol : currentMethod.getLocalSymbols()) {
			if (localSymbol.getName() == paramName) return true;
		}
		return false;
	}
	
	

	public void visit(Program program) { 
		nVars = Tab.currentScope.getnVars();
		outerScopeObj = program.getProgName().obj;
    	Tab.chainLocalSymbols(outerScopeObj);
    	Tab.closeScope();
    	if (!mainMethodDefined) {
    		report_info("Semanticka greska - u programu mora postojati metoda 'void main();'", program);
    	}
	}
	
	public void visit(ProgName progName){
    	progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	Tab.openScope();
    }
	
    public void visit(GlobalVarDeclaration globalVarDeclaration) {
		Struct type = globalVarDeclaration.getType().struct;
		for (var declVar : declarationVariables) {
	    	if (declVar.getArray()) {
	    		report_info("Deklarisan globalni niz '" + declVar.getName() + "'", globalVarDeclaration); 
	    		Tab.insert(Obj.Var, declVar.getName(), new Struct(Struct.Array, type));
	    	} else {
	    		report_info("Deklarisana globalna promenljiva '" + declVar.getName() + "'", globalVarDeclaration);
	    		Tab.insert(Obj.Var, declVar.getName(), type);
	    	}    	
    	}
    	declarationVariables.clear();
	}
    
    public void visit(ConstDeclaration constDeclaration) {	
		Struct type = constDeclaration.getType().struct;
		for (var declVar : declarationVariables) {
			report_info("Deklarisana konstanta '" + declVar.getName() + "'", constDeclaration);
			Tab.insert(Obj.Con, declVar.getName(), type);
		}
		declarationVariables.clear();
	}	

	public void visit(ConstPart constPart) { 
		if (Tab.find(constPart.getConstName()) == Tab.noObj) {
			String varName = constPart.getConstName();
			for (var declVar : declarationVariables) {
				if(declVar.getName().equals(varName)) {
					report_error("Semanticka greska - '" + constPart.getConstName() + "' je vec deklarisano", constPart);
					return;
				}
			}
			declarationVariables.add(new Variable(constPart.getConstName(), false, null));
		} else {			
			report_error("Semanticka greska - '" + constPart.getConstName() + "' je vec deklarisano", constPart);
		}
	}
	
	public void visit(MethodVoidName methodVoidName) { 
		currentMethod = Tab.insert(Obj.Meth, methodVoidName.getMethodName(), Tab.noType);
		methods.add(new Method(methodVoidName.getMethodName()));		
		Tab.openScope();
		if (methodVoidName.getMethodName().equals("main")) {
			mainMethodDefined = true;
		}
	}
	
	public void visit(MethodTypeName methodTypeName) { 
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethodName(), methodTypeName.getType().struct);
		 methods.add(new Method(methodTypeName.getMethodName()));		
		Tab.openScope();
	}
	
	public void visit(MethodVoidDeclaration methodVoidDeclaration) { 
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}
	
	public void visit(MethodTypeDeclaration methodTypeDeclaration) { 
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}
	
	public void visit(Type type) {
    	Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Semanticka greska - nije pronadjen tip '" + type.getTypeName() + "' u tabeli simbola!", null);
			type.struct = Tab.noType;
		} 
		else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} 
			else {
				report_error("Semanticka greska - '" + type.getTypeName() + "' ne predstavlja tip", type);
				type.struct = Tab.noType;
			}
		}
    }
	
	public void visit(VarDeclaration varDeclaration) {
		Struct type = varDeclaration.getType().struct;
		for (var declVar : declarationVariables) {
	    	if (declVar.getArray()) {
	    		report_info("Deklarisan niz '" + declVar.getName() + "'", varDeclaration); 
	    		Tab.insert(Obj.Var, declVar.getName(), new Struct(Struct.Array, type));
	    	} else {
	    		report_info("Deklarisana promenljiva '" + declVar.getName() + "'", varDeclaration);
	    		Tab.insert(Obj.Var, declVar.getName(), type);
	    	}    	
    	}
    	declarationVariables.clear();
	}
	
	public void visit(VarNormal varNormal) {
		if (Tab.find(varNormal.getVarName()) == Tab.noObj) {
			String varName = varNormal.getVarName();
			for (var declVar : declarationVariables) {
				if(declVar.getName().equals(varName)) {
					report_error("Semanticka greska - '" + varNormal.getVarName() + "' je vec deklarisano", varNormal);
					return;
				}
			}
			declarationVariables.add(new Variable(varNormal.getVarName(), false, null));			
		} else { 			
			report_error("Semanticka greska - '" + varNormal.getVarName() + "' je vec deklarisano", varNormal);
		}
	}
	
	public void visit(VarArray varArray) { 
		if (Tab.find(varArray.getVarName()) == Tab.noObj) {
			String varName = varArray.getVarName();
			for (var declVar : declarationVariables) {
				if(declVar.getName().equals(varName)) {
					report_error("Semanticka greska - '" + varArray.getVarName() + "' je vec deklarisano", varArray);
					return;
				}
			}
			declarationVariables.add(new Variable(varArray.getVarName(), true, null));			
		} else { 
			report_error("Semanticka greska - '" + varArray.getVarName() + "' je vec deklarisano", varArray);
		}
    }
		
	public void visit(ParamNormal paramNormal) {
		if (Tab.find(paramNormal.getParamName()) == Tab.noObj || !methodContains(paramNormal.getParamName())) {
	    	Obj obj = Tab.insert(Obj.Var, paramNormal.getParamName(), paramNormal.getType().struct);
	    	if (methods.size() > 0) {
				methods.get(methods.size() - 1).getArguments().add(obj.getType());
			}	
	    	if (currentMethod.getName().equals("main")) {
	    		mainMethodDefined = false;
	    	}
	    	currentMethod.setLevel(currentMethod.getLevel() + 1);
	    	report_info("Formalni parametar funkcije " + currentMethod.getName() + ": '" + paramNormal.getParamName() + "'", paramNormal);
		} else {
			report_error("Semanticka greska - '" + paramNormal.getParamName() + "' vec postoji", paramNormal);
		}
    }
	
	public void visit(ParamArray paramArray) {
		if (Tab.find(paramArray.getParamName()) == Tab.noObj || !methodContains(paramArray.getParamName())) {
			Obj obj = Tab.insert(Obj.Var, paramArray.getParamName(), new Struct(Struct.Array, paramArray.getType().struct));
			if (methods.size() > 0) {
				methods.get(methods.size() - 1).getArguments().add(obj.getType());
			}			
			if (currentMethod.getName().equals("main")) {
	    		mainMethodDefined = false;
	    	}
	    	currentMethod.setLevel(currentMethod.getLevel() + 1);
			report_info("Formalni parametar funkcije " + currentMethod.getName() + ": '" + paramArray.getParamName() + "'", paramArray);
		} else {
			report_error("Semanticka greska - '" + paramArray.getParamName() + "' vec postoji", paramArray);
		}
	}
	
	public void visit(DoKeyword doKeyword) {
		 doWhileDepth++;
	}
    
  	public void visit(DesignatorMethodCallParams designatorMethodCallParams){
    	Obj func = designatorMethodCallParams.getDesignator().obj;
    	if (Obj.Meth == func.getKind()) {
    		report_info("Pronadjen poziv funkcije '" + func.getName() + "'", designatorMethodCallParams);
    		Method method = null;
			for (var m : methods) {
				if (m.getName().equals(func.getName()))
					method = m;
			}
			if (method != null && !method.getArguments().equals(currentMethodParams)) {
				report_error("Semanticka greska - argumenti funkcije nisu odgovarajuci", designatorMethodCallParams);
			}
    	} else {
    		report_error("Semanticka greska - ime '" + func.getName() + "' nije funkcija", designatorMethodCallParams);
    	}
    	currentMethodParams.clear();
    }
	
	public void visit(DesignatorMethodCall designatorMethodCall){
    	Obj func = designatorMethodCall.getDesignator().obj;
    	if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv funkcije '" + func.getName() + "'", designatorMethodCall);
			Method method = null;
			for (var m : methods) {
				if (m.getName().equals(func.getName()))
					method = m;
			}
			if (method != null && method.getArguments().size() > 0) {
				report_error("Semanticka greska - argumenti funkcije nisu odgovarajuci", designatorMethodCall);
			}
    	} else {
			report_error("Semanticka greska - ime '" + func.getName() + "' nije funkcija", designatorMethodCall);
    	}
    	currentMethodParams.clear();
    }
	
	
	public void visit(DesignatorIncrement designatorIncrement) { 
		if (designatorIncrement.getDesignator().obj.getType() != Tab.intType) {
			report_error("Semanticka greska - tip za inkrement moze biti jedino int", designatorIncrement);
		}
	} 
	
	public void visit(DesignatorDecrement designatorDecrement) { 
		if (designatorDecrement.getDesignator().obj.getType() != Tab.intType) {
			report_error("Semanticka greska - tip za dekrement moze biti jedino int", designatorDecrement);
		}
	}
	
	public void visit(DesignatorAssignment designatorAssignment) {
 		Struct assignmentLeft = designatorAssignment.getDesignator().obj.getType();
 		int kind = designatorAssignment.getDesignator().obj.getKind();
 		if (kind != Obj.Var && kind != Obj.Elem) {
 			report_error("Semanticka greska - vrednost moze da se dodeli samo promenljivoj, elementu niza ili polju objekta", designatorAssignment);
 		}
 		if (assignmentRight != null && !assignmentRight.assignableTo(assignmentLeft)) {
 			report_error("Semanticka greska - nekompatibilni tipovi za dodelu vrednosti", designatorAssignment);
 		}
 		assignmentRight = null;
 	}
 	
 	public void visit(AssignmentExpr assignmentExpr) { 
 		assignmentRight = assignmentExpr.getExpr().struct;
 	}
  	
 	public void visit(ActParsSingle actParsSingle) {
 		currentMethodParams.add(actParsSingle.getExpr().struct);
 	}
 	
 	public void visit(ActParsMulti actParsMulti) {
 		currentMethodParams.add(actParsMulti.getExpr().struct);
 	}
 	
  	public void visit(IfCond ifCond) {
  		if (!ifCond.getCondition().struct.equals(boolType)) {
            report_error("Semanticka greska - if uslov nije tipa bool", ifCond);
        }
  	}
  	
   	public void visit(CondSingle condSingle) {
   		condSingle.struct = condSingle.getCondTerm().struct;
   	}
   	
   	public void visit(CondOr condOr) { // getCondition ili getCondTerm
   		condOr.struct = condOr.getCondTerm().struct;
   	}
   	
   	public void visit(CondTermSingle condTermSingle) {
   		condTermSingle.struct = condTermSingle.getCondFact().struct;
   	}
   	
   	public void visit(CondTermAnd condTermAnd) { // CondFact ili condTerm
   		condTermAnd.struct = condTermAnd.getCondFact().struct;
   	}
   	
   	public void visit(CondFactSingle condFactSingle) {
   		condFactSingle.struct = condFactSingle.getExpr().struct;
   	}
   	
   	public void visit(CondFactRelop condFactRelop) {
   		if (!condFactRelop.getExpr().struct.compatibleWith(condFactRelop.getExpr1().struct)) {
            report_error("Semanticka greska - tipovi relacionog izraza nisu kompatibilni", condFactRelop);
        } else {
        	int kindLeft = condFactRelop.getExpr().struct.getKind();
        	int kindRight = condFactRelop.getExpr1().struct.getKind();
        	Relop relop = condFactRelop.getRelop();
            if (kindLeft == Struct.Array || kindRight == Struct.Array) {
            	if (!(relop instanceof RelopEQ || relop instanceof RelopNE)) {
            		report_error("Semanticka greska - relacioni izraz sa referentnim tipovima moze koristiti samo '==' i '!=' operatore", condFactRelop);
                }
            }
        }
   		condFactRelop.struct = boolType;
   	}
	
	public void visit(ExprNeg exprNeg) { 
		if (exprNeg.getTerm().struct.getKind() != Struct.Int) {
			report_error("Semanticka greska - tip mora da bude int", exprNeg);
		}
		exprNeg.struct = exprNeg.getTerm().struct;
	}
	
	public void visit(ExprSingle exprSingle) { 
		exprSingle.struct = exprSingle.getTerm().struct;
	}
	
	public void visit(ExprAddop exprAddop) {
		if (!exprAddop.getExpr().struct.equals(Tab.intType) || !exprAddop.getTerm().struct.equals(Tab.intType)) {
            report_error("Semanticka greska - clanovi izraza nisu tipa int", exprAddop);
        }
		if (!exprAddop.getExpr().struct.compatibleWith(exprAddop.getTerm().struct)) {
            report_error("Semanticka greska - clanovi izraza nisu kompatibilni", exprAddop);
        }
		exprAddop.struct = exprAddop.getExpr().struct;
	}
	
	public void visit(TermMulop termMulop) {
		if (!termMulop.getTerm().struct.equals(Tab.intType) || !termMulop.getFactor().struct.equals(Tab.intType)) {
            report_error("Semanticka greska - clanovi izraza nisu tipa int", termMulop);
        }
		termMulop.struct = termMulop.getTerm().struct;
	}
	
	public void visit(TermSingle termSingle) { 
		termSingle.struct = termSingle.getFactor().struct;
	}
	
	public void visit(FuncCallParams funcCallParams){
    	Obj func = funcCallParams.getDesignator().obj;
    	if (Obj.Meth == func.getKind()) {
    		report_info("Pronadjen poziv funkcije '" + func.getName() + "'", funcCallParams);
    		funcCallParams.struct = func.getType();
    		Method method = null;
			for (var m : methods) {
				if (m.getName().equals(func.getName()))
					method = m;
			}
			if (method != null && !method.getArguments().equals(currentMethodParams)) {
				report_error("Semanticka greska - argumenti funkcije nisu odgovarajuci", funcCallParams);
			}
    	} else {
    		report_error("Semanticka greska - ime '" + func.getName() + "' nije funkcija", funcCallParams);
			funcCallParams.struct = Tab.noType;
    	}
    	currentMethodParams.clear();
    }
	
	public void visit(FuncCall funcCall){
    	Obj func = funcCall.getDesignator().obj;
    	if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv funkcije '" + func.getName() + "'", funcCall);
			funcCall.struct = func.getType();
			Method method = null;
			for (var m : methods) {
				if (m.getName().equals(func.getName()))
					method = m;
			}
			if (method != null && method.getArguments().size() > 0) {
				report_error("Semanticka greska - argumenti funkcije nisu odgovarajuci", funcCall);
			}
    	} else {
			report_error("Semanticka greska - ime '" + func.getName() + "' nije funkcija", funcCall);
			funcCall.struct = Tab.noType;
    	}
    	currentMethodParams.clear();
    }
	
	public void visit(FactorDesignator factorDesignator) {
		 factorDesignator.struct = factorDesignator.getDesignator().obj.getType();
	}
	
	public void visit(FactorNumConst factorNumConst) { 
		factorNumConst.struct = Tab.intType;
	}
	
	public void visit(FactorCharConst factorCharConst) { 
		factorCharConst.struct = Tab.charType;
	}
	
	public void visit(FactorBoolConst factorBoolConst) { 
		factorBoolConst.struct = boolType;
	}

	public void visit(FactorNew factorNew) {
		factorNew.struct = factorNew.getType().struct;
	} 
	
	public void visit(FactorNewArray factorNewArray) {
		if (factorNewArray.getExpr().struct == Tab.intType) {
			factorNewArray.struct = new Struct(Struct.Array, factorNewArray.getType().struct);			
		} else {
			report_error("Semanticka greska - tip za alociranje niza moze biti jedino int", factorNewArray);
		}
	}
	
	public void visit(FactorExpr factorExpr) { 
		factorExpr.struct = factorExpr.getExpr().struct;
	}
	
	public void visit(DesignatorArray designatorArray) {
		Obj obj = Tab.find(designatorArray.getDesignatorName());
		if (obj == Tab.noObj) { 
			report_error("Semanticka greska - niz '" + designatorArray.getDesignatorName() + "' nije deklarisan", designatorArray);
		}
		if (designatorArray.getExpr().struct != Tab.intType) {
			report_error("Semanticka greska - nevalidan pristup elementu niza", designatorArray);
		} else if (obj.getType().getKind() != Struct.Array){
			report_error("Semanticka greska - '" + designatorArray.getDesignatorName() + "' nije niz", designatorArray);
		}
		designatorArray.obj = new Obj(Obj.Elem, obj.getName(), obj.getType().getElemType());
		report_info("Pristup elementu niza '" + designatorArray.getDesignatorName() + "'", designatorArray);
	} 
	
	public void visit(DesignatorSimple designatorSimple) { 
		Obj obj = Tab.find(designatorSimple.getDesignatorName());
		if (obj == Tab.noObj) { 
			report_error("Semanticka greska - '" + designatorSimple.getDesignatorName() + "' nije deklarisano", designatorSimple);
		}		
		designatorSimple.obj = obj;
	}
	
	public void visit(NumConst numConst) {
		numConst.struct = Tab.intType;
	}
	
	public void visit(BoolConst boolConst) { 
		boolConst.struct = boolType;
	}
	
	public void visit(CharConst charConst) { 
		charConst.struct = Tab.charType;
	}
	
	public void visit(StmtDoWhile stmtDoWhile) {
		 if (!stmtDoWhile.getCondition().struct.equals(boolType)) {
             report_error("Semanticka greska - while uslov nije tipa bool", stmtDoWhile);
         }
         doWhileDepth--;
	}
	
	public void visit(StmtBreak stmtBreak) {
		 if (doWhileDepth == 0) {
             report_error("Semanticka greska - break iskaz van petlje", stmtBreak);
         }
    }
	
	public void visit(StmtContinue stmtContinue) {
		 if (doWhileDepth == 0) {
            report_error("Semanticka greska - continue iskaz van petlje", stmtContinue);
        }
	}
	
    public void visit(StmtReturnExpr stmtReturnExpr) {
    	if (currentMethod.getType() == Tab.noType) {
    		report_error("Semanticka greska - return naredba u funkciji koja nema povratnu vrednost", stmtReturnExpr);
    		return;
    	}
    	if (!currentMethod.getType().compatibleWith(stmtReturnExpr.getExpr().struct)) {
    		report_error("Semanticka greska - tip povratne vrednosti metode i tip vrednosti izraza u return naredbi se ne slazu", stmtReturnExpr);
    		return;
    	}
    }
    
    public void visit(StmtRead stmtRead) {
		Obj obj = stmtRead.getDesignator().obj;
		if (obj.getKind() == Obj.Var || obj.getKind() == Obj.Elem) {
			Struct type = obj.getType();
			if (!type.equals(Tab.intType) && !type.equals(Tab.charType) && !type.equals(boolType)) {
				report_error("Semanticka greska - izraz u read naredbi mora biti int, char ili bool tipa", stmtRead);
			}
		} else {
			report_error("Semanticka greska - argument read naredbe nije promenljiva ili element niza", stmtRead);
		}
	}

  	public void visit(StmtPrintNumConst stmtPrintNumConst) {
  		Struct expr = stmtPrintNumConst.getExpr().struct;
  		if (expr != null && !expr.equals(Tab.intType) && !expr.equals(Tab.charType) && !expr.equals(boolType)) {
  			report_error("Semanticka greska - izraz print naredbe nije int, char ili bool tipa", stmtPrintNumConst);
  		}
  	}

  	public void visit(StmtPrint stmtPrint) {	
  		Struct expr = stmtPrint.getExpr().struct;
  		if (expr != null && !expr.equals(Tab.intType) && !expr.equals(Tab.charType) && !expr.equals(boolType)) {
  			report_error("Semanticka greska - izraz print naredbe nije int, char ili bool tipa", stmtPrint);
  		}
  	} 
}
