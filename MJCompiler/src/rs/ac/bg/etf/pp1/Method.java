package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.etf.pp1.symboltable.concepts.Struct;

public class Method {
	private String name;
	private ArrayList<Struct> arguments; 
			
	public Method(String name) {		
		this.name = name;
		this.arguments = new ArrayList<Struct>();
	}				
	
	public Method(String name, ArrayList<Struct> parameters) {		
		this.name = name;
		this.arguments = parameters;
	}				

	public String getName() {
		return name;
	}
	
	public ArrayList<Struct> getArguments() {
		return arguments;
	}		
}
