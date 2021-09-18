package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class TableDumpVisitor extends SymbolTableVisitor {
	
	private static final String INDENT = "   ";
	
	private StringBuilder output = new StringBuilder();
	private StringBuilder currentIndent = new StringBuilder();
	
	private void addIndent() {
		currentIndent.append(INDENT);
	}
	
	private void removeIndent() {
		if (currentIndent.length() > 0)
			currentIndent.setLength(currentIndent.length()- INDENT.length());
	}
	
	private void addNewLine() {
		output.append("\n");
	}
	
	private String objKindToString(Obj node) {
		String retVal = "";
		if (node.getKind() == Obj.Con) {
			retVal = "Con";
		}
		else if (node.getKind() == Obj.Var) {
			retVal = "Var";
		}
		else if (node.getKind() == Obj.Type) {
			retVal = "Type";
		}
		else if (node.getKind() == Obj.Meth) {
			retVal = "Meth";
		}
		else if (node.getKind() == Obj.Fld) {
			retVal = "Fld";
		}
		else if (node.getKind() == Obj.Prog) {
			retVal = "Prog";
		}
		
		return retVal + " ";
	}
	
	@Override
	public void visitObjNode(Obj currentNode) {
		output.append(objKindToString(currentNode));
		output.append(currentNode.getName());
		output.append(": ");
		
		if ((Obj.Var == currentNode.getKind()) && "this".equalsIgnoreCase(currentNode.getName())) {
			output.append("");
		}
		else {
			currentNode.getType().accept(this);
		}
		
		output.append(", ");
		output.append(currentNode.getAdr());
		output.append(", ");
		output.append(currentNode.getLevel() + " ");
				
		if (currentNode.getKind() == Obj.Prog || currentNode.getKind() == Obj.Meth) {
			addNewLine();
			addIndent();
		}

		for (var localSymbol : currentNode.getLocalSymbols()) {
			output.append(currentIndent.toString());
			localSymbol.accept(this);
			addNewLine();
		}
		
		if (currentNode.getKind() == Obj.Prog || currentNode.getKind() == Obj.Meth) {
			removeIndent();		
		}
	}

	@Override
	public void visitScopeNode(Scope scope) {
		for (var value : scope.values()) {
			value.accept(this);
			addNewLine();
		}
	}

	@Override
	public void visitStructNode(Struct structToVisit) {
		switch (structToVisit.getKind()) {
		case Struct.None:
			output.append("notype");
			break;
		case Struct.Int:
			output.append("int");
			break;
		case Struct.Bool:
			output.append("bool");
			break;
		case Struct.Char:
			output.append("char");
			break;
		case Struct.Array:
			output.append("Arr of ");
			
			switch (structToVisit.getElemType().getKind()) {
			case Struct.None:
				output.append("notype");
				break;
			case Struct.Int:
				output.append("int");
				break;
			case Struct.Bool:
				output.append("bool");
				break;
			case Struct.Char:
				output.append("char");
				break;
			case Struct.Class:
				output.append("Class");
				break;
			}
			break;
		case Struct.Class:
			output.append("Class [");
			for (Obj obj : structToVisit.getMembers()) {
				obj.accept(this);
			}
			output.append("]");
			break;
		}

	}

	public String getOutput() {
		return output.toString();
	}

}
