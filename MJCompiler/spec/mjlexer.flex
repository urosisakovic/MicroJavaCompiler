
package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// Ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline + 1, yycolumn);
	}
	
	// Ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline + 1, yycolumn, value);
	}
	
	private void report_lexer_error() {
		MJCompiler.getInstance().reportError(new CompilerError(
				yyline + 1,
				"Leksicka greska (" + yytext() + ") na liniji " + (yyline + 1) + " i koloni " + (yycolumn + 1),
				CompilerError.CompilerErrorType.LEXICAL_ERROR));
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }

"program"   { return new_symbol(sym.PROGRAM);}
"break"     { return new_symbol(sym.BREAK);}
"case" 		{ return new_symbol(sym.CASE); }
"class"     { return new_symbol(sym.CLASS);}
"continue" 	{ return new_symbol(sym.CONTINUE); }
"const"     { return new_symbol(sym.CONST);}
"do"        { return new_symbol(sym.DO);}
"else"      { return new_symbol(sym.ELSE);}
"enum"      { return new_symbol(sym.ENUM);}
"if"        { return new_symbol(sym.IF);}
"new"       { return new_symbol(sym.NEW);}
"print"     { return new_symbol(sym.PRINT);}
"read"      { return new_symbol(sym.READ);}
"return"    { return new_symbol(sym.RETURN);}
"switch"    { return new_symbol(sym.SWITCH);}
"void"      { return new_symbol(sym.VOID);}
"while"     { return new_symbol(sym.WHILE);}

"+" 		{ return new_symbol(sym.PLUS); }
"-" 		{ return new_symbol(sym.MINUS); }
"*" 		{ return new_symbol(sym.MUL); }
"/" 		{ return new_symbol(sym.DIV); }
"%" 		{ return new_symbol(sym.MOD); }
"=" 		{ return new_symbol(sym.ASSIGNMENT); }
"==" 		{ return new_symbol(sym.EQUAL); }
"!=" 		{ return new_symbol(sym.NOT_EQUAL); }
">"			{ return new_symbol(sym.GREATER); }
">=" 		{ return new_symbol(sym.GREATER_EQUAL); }
"<" 		{ return new_symbol(sym.LESS); }
"<=" 		{ return new_symbol(sym.LESS_EQUAL); }
"&&" 		{ return new_symbol(sym.AND); }
"||" 		{ return new_symbol(sym.OR); }
"++" 		{ return new_symbol(sym.INC); }
"--"		{ return new_symbol(sym.DEC); }
";" 		{ return new_symbol(sym.SEMICOLON); }
":"			{ return new_symbol(sym.COLON); }
","			{ return new_symbol(sym.COMMA); }
"." 		{ return new_symbol(sym.FULL_STOP); }
"(" 		{ return new_symbol(sym.LPAREN); }
")" 		{ return new_symbol(sym.RPAREN); }
"[" 		{ return new_symbol(sym.LSQUARE); }
"]" 		{ return new_symbol(sym.RSQUARE); }
"{" 		{ return new_symbol(sym.LBRACE); }
"}" 		{ return new_symbol(sym.RBRACE); }

"//" { yybegin(COMMENT);}
<COMMENT> "\r\n" { yybegin(YYINITIAL); }
<COMMENT> . { yybegin(COMMENT); }

"true" | "false" {return new_symbol(sym.BOOL_CONST, new Boolean(yytext()));}
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* {return new_symbol(sym.IDENT, yytext());}
[0-9]+ {return new_symbol(sym.NUM_CONST, new Integer(yytext()));}
"'"[\040-\176]"'" {return new_symbol(sym.CHAR_CONST, new Character(yytext().charAt(1)));}

. {
	report_lexer_error();
}
