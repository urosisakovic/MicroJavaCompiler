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
	
	private String structKindToString(Struct struct) {
		switch (struct.getKind()) {
			case Struct.None: {
				return "notype";
			}
			case Struct.Int: {
				return "int";
			}
			case Struct.Bool: {
				return "bool";
			}
			case Struct.Char: {
				return "char";
			}
			case Struct.Class: {
				output.append("Class [");
				for (var member: struct.getMembers()) {
					member.accept(this);
				}
				output.append("]");
				break;
			}
			case Struct.Array: {
				String retVal = "Arr of ";
				
				switch (struct.getElemType().getKind()) {
					case Struct.None:
						return retVal + "notype";
					case Struct.Int:
						return retVal + "int";
					case Struct.Bool:
						return retVal + "bool";
					case Struct.Char:
						return retVal + "char";
					case Struct.Class:
						return retVal + "class";
				}
			}
		}
		return "";
	}

	@Override
	public void visitStructNode(Struct currentStruct) {
		output.append(structKindToString(currentStruct));
	}

	public String getOutput() {
		return output.toString();
	}

}
