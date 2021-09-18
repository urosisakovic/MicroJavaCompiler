package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(getClass());
	
	private int mainPc;
	private Obj outerScope = null;
	
	private Obj currentMethod = null;
	
	private Variable currentVariable = null;
	private List<Variable> declaredVariables = new ArrayList<Variable>();

	private int condCnt = 0;
	
	private static final int INTEGER_PRINT_WIDTH = 5;
	private static final int CHAR_PRINT_WIDTH = 1;
	
	private Stack<Integer> relopOperationStack;
	private Stack<Integer> addopOperationStack;
	private Stack<Integer> mulopOperationStack;
	
	private LinkedList<ArrayList<JumpCondition>> ifConditionsStack = new LinkedList<ArrayList<JumpCondition>>();
	private LinkedList<Integer> ifPcStack = new LinkedList<Integer>(); 
	private LinkedList<Integer> retWhilePcStack = new LinkedList<Integer>();
	private LinkedList<ArrayList<Integer>> breakPcStack = new LinkedList<ArrayList<Integer>>();
	private LinkedList<ArrayList<Integer>> continuePcStack = new LinkedList<ArrayList<Integer>>();
	private LinkedList<ArrayList<JumpCondition>> whileCondsStack = new LinkedList<ArrayList<JumpCondition>>();

	public CodeGenerator(Obj outerScope) {
		super();
		this.outerScope = outerScope;
		
		relopOperationStack = new Stack<>();
		addopOperationStack = new Stack<>();
		mulopOperationStack = new Stack<>();
	}
	
	public int getMainPc() {
		return mainPc;
	}
	
	public void report_error(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		if (info != null && info.getLine() != 0) {
			msg.append(" na liniji ").append(info.getLine());
		}
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		if (info != null && info.getLine() != 0) {
			msg.append(" na liniji ").append(info.getLine());
		}
		log.info(msg.toString());
	}
	
	public Obj getMethodByName(String methodName) {
		var methodObject = Tab.find(methodName);
		
		if (methodObject != Tab.noObj) {
			return methodObject;
		}

		for (var localSymbol : outerScope.getLocalSymbols()) {
			if (localSymbol.getKind() == Obj.Meth && localSymbol.getName().equals(methodName)) {
				return localSymbol;
			}
		}

		return null;
	}
	
	public Obj getVarOrConstByName(String varName) {
		var varObject = Tab.find(varName);

		if (varObject != Tab.noObj) {
			return varObject;
		}
		
		for (var localSymbol : outerScope.getLocalSymbols()) {				
			if (localSymbol.getKind() != Obj.Meth && localSymbol.getName().equals(varName)) {
				return localSymbol;
			}
		}			
		if (currentMethod != null) {
			for (var localSymbol: currentMethod.getLocalSymbols()) {		
				if (localSymbol.getName().equals(varName)) {
					return localSymbol;
				}
			}
		}		

		return null;
	}
	
	public void fixedAddressJump(JumpCondition condition, int jumpAddress) {
		Code.put2(condition.getPc(), jumpAddress - condition.getPc() + 3);
		condition.setModified(true);
	}
	
	public void fixedJumpCondition(JumpCondition condition) {
		int oldPc = Code.pc;
		Code.pc = condition.getPc() - 1;

		if (condition.getRelop() == -1) { 
			Code.put(Code.jcc + Code.ne);
		}
		else {
			Code.put(Code.jcc + condition.getRelop());
		}
		
		Code.pc = oldPc;
		Code.put2(condition.getPc(), Code.pc - condition.getPc() + 1);
		condition.setModified(true);
	}
	
	private void jumpAndSavePc(boolean isDoWhile, int ordNum, int relOp) {
		if (!isDoWhile) {
			if (relOp == - 1) {
				Code.loadConst(0);
				Code.put(Code.jcc + Code.eq);
			} else {
				Code.put(Code.jcc + Code.inverse[relOp]);
			}
			ArrayList<JumpCondition> markedConds = ifConditionsStack.getLast();
			markedConds.add(new JumpCondition(Code.pc, ordNum, relOp));
			Code.pc += 2;
		} else {
			if (relOp == - 1) {
				Code.loadConst(0);
				Code.put(Code.jcc + Code.ne);
			} else {
				Code.put(Code.jcc + relOp);
			}	
			ArrayList<JumpCondition> whileConds = whileCondsStack.getLast();
			whileConds.add(new JumpCondition(Code.pc, ordNum, relOp));
			Code.put2(retWhilePcStack.getLast() - Code.pc + 1);	
		}
	}
	
	private SyntaxNode prepare(SyntaxNode syntaxNode) {
		while (syntaxNode.getClass() != StmtIf.class
				&& syntaxNode.getClass() != StmtIfElse.class
				&& syntaxNode.getClass() != StmtDoWhile.class
				&& syntaxNode != null) {
			syntaxNode = syntaxNode.getParent();
		}		
		return syntaxNode;
	}	
	
	public void visit(Program program) {		
		Code.dataSize = outerScope.getLocalSymbols().size();
	}
	
	public void visit(ConstDeclaration constDeclaration) {
		for (var variable: declaredVariables) {
			Obj o = getVarOrConstByName(variable.getName());			
			if (o != null) {
				Object val = variable.getValue();
				if (val instanceof Integer) {
					o.setAdr((Integer) val);
				} else if (val instanceof Character) {
					o.setAdr((Character) val);
				} else if (val instanceof Boolean) {				
					o.setAdr((Boolean) val ? 1 : 0);
				} else {
					o.setAdr(0);
				}
				Code.load(o);
			}
		}
		declaredVariables.clear();
	}	
	
	public void visit(ConstPart constPart) {
		currentVariable.setName(constPart.getConstName());
		declaredVariables.add(currentVariable);
		currentVariable = null;
	}
	
	public void visit(MethodVoidDeclaration methodVoidDeclaration) {
		Code.put(Code.exit); 
		Code.put(Code.return_);
		currentMethod = null;
	}
	
	public void visit(MethodVoidName methodVoidName) {
		currentMethod = getMethodByName(methodVoidName.getMethodName());	

		if (methodVoidName.getMethodName().equals("main")) {
			mainPc = Code.pc;			
		}

		currentMethod.setAdr(Code.pc);
        Code.put(Code.enter);
        Code.put(currentMethod.getLevel());
        Code.put(currentMethod.getLocalSymbols().size());               
	}
	
	public void visit(MethodTypeDeclaration methodTypeDeclaration) {
		Code.put(Code.trap); 
		Code.put(1);
		currentMethod = null;
	}
	
	public void visit(MethodTypeName methodTypeName) { 		
		currentMethod = getMethodByName(methodTypeName.getMethodName());
		currentMethod.setAdr(Code.pc);
        Code.put(Code.enter);
        Code.put(currentMethod.getLevel());
        Code.put(currentMethod.getLocalSymbols().size());
	}
	
	public void visit(NumConst numConst) {
		currentVariable = new Variable("", false, numConst.getN1());
	}
	
	public void visit(BoolConst boolConst) { 
		currentVariable = new Variable("", false, boolConst.getB1());
	}    
	
	public void visit(CharConst charConst) { 
		currentVariable = new Variable("", false, charConst.getC1());
	}	
	
	public void visit(DesignatorAssignment designatorAssignment) {
		Code.store(designatorAssignment.getDesignator().obj);	
	}
	
	public void visit(StmtPrint stmtPrint) {
		Struct struct = stmtPrint.getExpr().struct;
		if (struct.equals(Tab.intType) || struct.equals(SemanticAnalyzer.boolType)) {
			Code.loadConst(INTEGER_PRINT_WIDTH);
			Code.put(Code.print);
		} else if (struct.equals(Tab.charType)) {
			Code.loadConst(CHAR_PRINT_WIDTH);
			Code.put(Code.bprint);
		}	
	}
	
	public void visit(StmtPrintNumConst stmtPrintNumConst) {
		Struct struct = stmtPrintNumConst.getExpr().struct;
		if (struct.equals(Tab.intType) || struct.equals(SemanticAnalyzer.boolType)) {
			Code.loadConst(stmtPrintNumConst.getN2());
			Code.put(Code.print);
		} else if (struct.equals(Tab.charType)) {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}		
	}
	
	public void visit(StmtRead stmtRead) {		
		Obj obj = stmtRead.getDesignator().obj;
        if (obj.getType().equals(Tab.charType)) {
            Code.put(Code.bread);
        }
        else { 
            Code.put(Code.read);
        }
        Code.store(obj);
	}
	
	public void visit(StmtReturnExpr stmtReturnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(StmtReturn stmtReturn) { 
		Code.put(Code.exit);
		Code.put(Code.return_);
	}  
	
	public void visit(StmtIf stmtIf) {
		ArrayList<JumpCondition> ifConds = ifConditionsStack.removeLast();
		for (JumpCondition ifCond : ifConds) {
			if (!ifCond.isModified()) {
				Code.put2(ifCond.getPc(), Code.pc - ifCond.getPc() + 1);
			}
		}
		ifConds.clear();
	}	
	
	public void visit(StmtIfElse stmtIfElse) {
		ArrayList<JumpCondition> ifConds = ifConditionsStack.removeLast();
		// Last in first out
		int elsePc = ifPcStack.removeLast();
		for (int i = 0; i < ifConds.size(); i++) {
			if (!ifConds.get(i).isModified()) {
				Code.put2(ifConds.get(i).getPc(),  elsePc - ifConds.get(i).getPc() + 3);
			}
		}						
		ifConds.clear();
		Code.put2(elsePc, Code.pc - elsePc + 1);
	}	
	
	public void visit(IfKeyword ifKeyword) {
		ifConditionsStack.add(new ArrayList<JumpCondition>());
	}	
	
	public void visit(StmtIfBody stmtIfBody) {		
		if (stmtIfBody.getParent().getClass() == StmtIfElse.class) {
			Code.put(Code.jmp);
			ifPcStack.addLast(Code.pc);
			Code.pc += 2;
		}
	}
	
	public void visit(StmtDoWhile stmtDoWhile) {
		retWhilePcStack.removeLast();
		ArrayList<Integer> breakPcs = breakPcStack.removeLast();
		for (int i = 0; i < breakPcs.size(); i++) {			
			// TODO: Fix address
			Code.put2(breakPcs.get(i), Code.pc - breakPcs.get(i) + 1);							
		}
		// TODO: Fix address for the last CondTerm
		ArrayList<JumpCondition> whileConds = whileCondsStack.removeLast();
		int lastCondTerm = 0;
		for (int i = 0; i < whileConds.size() - 1; i++) {
			if (whileConds.get(i).getOrdNum() == 0 && !whileConds.get(i).isModified()) {
				lastCondTerm = i;
			}
		}
		for (int i = lastCondTerm; i < whileConds.size() - 1; i++) {
			fixedJumpConditionWhile(whileConds.get(i), Code.pc - 2);
		}
	}
	
	public void visit(DoKeyword doKeyword) {
		retWhilePcStack.addLast(Code.pc);
		breakPcStack.add(new ArrayList<Integer>());
		continuePcStack.add(new ArrayList<Integer>());
		whileCondsStack.add(new ArrayList<JumpCondition>());
	}
	
	public void visit(StmtWhileBody stmtWhileBody) {
		ArrayList<Integer> continuePcs = continuePcStack.removeLast();
		for (int i = 0; i < continuePcs.size(); i++) {			
			Code.put2(continuePcs.get(i), Code.pc - continuePcs.get(i) + 1);						
		}
	}
		
    public void visit(StmtBreak stmtBreak) {    	
    	Code.put(Code.jmp);  	
    	breakPcStack.getLast().add(Code.pc);
    	Code.pc += 2;
    }
	
    public void visit(StmtContinue stmtContinue) {     	 
    	Code.put(Code.jmp);
    	continuePcStack.getLast().add(Code.pc);
    	Code.pc += 2;
    }
	
	public void visit(DesignatorIncrement designatorIncrement) {
		if (designatorIncrement.getDesignator().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		Code.load(designatorIncrement.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(designatorIncrement.getDesignator().obj);		
	}
	
	public void visit(DesignatorDecrement designatorDecrement) {
		if(designatorDecrement.getDesignator().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		Code.load(designatorDecrement.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(designatorDecrement.getDesignator().obj);		
	}
	
	public void visit(DesignatorMethodCallParams designatorMethodCallParams) {
		Obj method = designatorMethodCallParams.getDesignator().obj;
		if (outerScope.getLocalSymbols().contains(method)) {
			int offset = method.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
			if (method.getType() != Tab.noType )
				Code.put(Code.pop); // rezultat poziva nece biti koriscen
		}
	}

	public void visit(DesignatorMethodCall designatorMethodCall) { 
		Obj method = designatorMethodCall.getDesignator().obj;
		if (outerScope.getLocalSymbols().contains(method)) {
			int offset = method.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
			if (method.getType() != Tab.noType ) // skida povratnu vrednost
				Code.put(Code.pop);
		}
	}
	
	public void visit(IfCond ifCond) {
		ArrayList<JumpCondition> markedConds = ifConditionsStack.getLast();
		int startOfLastCondTerm = 0;
		for (int i = markedConds.size() - 1; i >= 0; i--) {
			if (markedConds.get(i).getOrdNum() == 0) {
				startOfLastCondTerm = i;
				break;
			}
		}
		for (int i=0; i<startOfLastCondTerm; i++) {
			if (markedConds.get(i).isModified() == false) {
				fixedJumpCondition(markedConds.get(i));
			}
		}
	}
	
	public void visit(CondOr condOr) {	
		condCnt = 0;

		SyntaxNode stmt = prepare(condOr);

		if (stmt.getClass() != StmtDoWhile.class) {
			ArrayList<JumpCondition> markedConditions = ifConditionsStack.getLast();
			int startOfLastCondTerm = 0;
			int startOfCurrentCondTerm = 0;
			
			for (int i = markedConditions.size() - 1; i >= 0; i--) {
				if (markedConditions.get(i).getOrdNum() == 0) {
					startOfLastCondTerm = i;
					break;
				}
			}
			for (int i = 0; i < startOfLastCondTerm; i++) {
				if (markedConditions.get(i).getOrdNum() == 0 && markedConditions.get(i).isModified() == false) {
					startOfCurrentCondTerm = i;
					break;
				}
			}
			for (int j = startOfCurrentCondTerm; j < startOfLastCondTerm - 1; j++) {
				fixedAddressJump(markedConditions.get(j), markedConditions.get(startOfLastCondTerm - 1).getPc());
			}
		} else {
			ArrayList<JumpCondition> markedConds = whileCondsStack.getLast();
			int startOfLastCondTerm = 0;
			int startOfCurrentCondTerm = 0;
			
			for (int i = markedConds.size() - 1; i >= 0; i--) {
				if (markedConds.get(i).getOrdNum() == 0) {
					startOfLastCondTerm = i;
					break;
				}
			}
			for (int i = 0; i < startOfLastCondTerm; i++) {
				if (markedConds.get(i).getOrdNum() == 0 && markedConds.get(i).isModified() == false) {
					startOfCurrentCondTerm = i;
					break;
				}
			}
			for (int j = startOfCurrentCondTerm; j < startOfLastCondTerm - 1; j++) {
				fixedJumpConditionWhile(markedConds.get(j), markedConds.get(startOfLastCondTerm - 1).getPc());
			}
		}
	}		
	
	public void fixedJumpConditionWhile(JumpCondition condition, int jumpPc) {
		int oldPc = Code.pc;
		Code.pc = condition.getPc() - 1;
		if (condition.getRelop() == -1) {
			Code.put(Code.jcc + Code.eq);
		}
		else {
			Code.put(Code.jcc + Code.inverse[condition.getRelop()]);
		}
		Code.pc = oldPc;
		Code.put2(condition.getPc(), jumpPc - condition.getPc() + 3);
		condition.setModified(true);
	}
	
	public void visit(CondSingle condSingle) {
		condCnt = 0;
	}
		
    public void visit(CondTermAnd condTermAnd) {
		condCnt++; 
    }
    
	public void visit(CondTermSingle condTermSingle) {
		condCnt++; 
	}
	
	public void visit(CondFactRelop condFactRelop) {	
		SyntaxNode stmt = prepare(condFactRelop);
		jumpAndSavePc(stmt.getClass() == StmtDoWhile.class, condCnt, relopOperationStack.pop());
	}
	
	public void visit(CondFactSingle condFactSingle) {		
		SyntaxNode stmt = prepare(condFactSingle);
		jumpAndSavePc(stmt.getClass() == StmtDoWhile.class, condCnt, -1);
	}
	
	public void visit(ExprAddop exprAddop) {
        Code.put(addopOperationStack.pop());
    }
	
	public void visit(ExprNeg exprNeg) {
        Code.put(Code.neg);
    }
	
	public void visit(TermMulop termMulop) {
		Code.put(mulopOperationStack.pop());
	}
	
	public void visit(FactorNumConst factorNumConst) {
		Code.loadConst(factorNumConst.getN1());
	}
	
	public void visit(FactorCharConst factorCharConst) {
		Code.loadConst(factorCharConst.getC1());
	}
	
	public void visit(FactorBoolConst factorBoolConst) {
		Code.loadConst(factorBoolConst.getB1() ? 1 : 0);		
	}
	
	public void visit(FactorNewArray factorNewArray) {		
		Code.put(Code.newarray);
        if (factorNewArray.getType().struct == Tab.charType) {
			Code.put(0); 
        } else { 
			Code.put(1);
        }        
	}
	
	public void visit(FactorDesignator factorDesignator) {
		Obj o = getVarOrConstByName(factorDesignator.getDesignator().obj.getName());
		if (o != null) {
			Code.load(factorDesignator.getDesignator().obj);
		}
	}
	
	public void visit(FuncCallParams funcCallParams) {
		Obj method = funcCallParams.getDesignator().obj;
		if (outerScope.getLocalSymbols().contains(method)) {
			int offset = method.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
			return;
		}
		if (method.getName().equals("len")) {
			Code.put(Code.arraylength);
		}
	}

	public void visit(FuncCall funcCall) {
		Obj method = funcCall.getDesignator().obj;
		if (outerScope.getLocalSymbols().contains(method)) {
			int offset = method.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(offset);
		}
	}
	
	public void visit(DesignatorArray designatorArray) {
		var varObject = getVarOrConstByName(designatorArray.getDesignatorName());	
		if (varObject != null) {
			Code.load(varObject);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
		}	
	}
	
	public void visit(AddopPlus addopPlus) {
		addopOperationStack.push(Code.add);
	}
	
	public void visit(AddopMinus addopMinus) {
		addopOperationStack.push(Code.sub);
	}
	
	public void visit(MulopMul mulopMul) {		
		mulopOperationStack.push(Code.mul);
	}

	public void visit(MulopDiv mulopDiv) {
		mulopOperationStack.push(Code.div);
	}
	
	public void visit(MulopMod mulopMod) {
		mulopOperationStack.push(Code.rem);
	}
	
	public void visit(RelopEQ relopEQ) { 
		relopOperationStack.push(Code.eq); 		
	}
	
	public void visit(RelopNE relopNE) { 
		relopOperationStack.push(Code.ne); 	
	}
	
	public void visit(RelopGT relopGT) { 
		relopOperationStack.push(Code.gt); 	
	}
	
	public void visit(RelopGE relopGE) {
		relopOperationStack.push(Code.ge); 	
	}
	
	public void visit(RelopLT relopLT) { 
		relopOperationStack.push(Code.lt); 	
	}
	
    public void visit(RelopLE relopLE) { 
    	relopOperationStack.push(Code.le);
    }
}
