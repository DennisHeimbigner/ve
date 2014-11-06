/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

%require "3.0" /* or later */
%language "Java"
%debug
%error-verbose
%locations

%define api.push-pull pull
%define api.position.type {VELexer.Position}
%define package {ucar.ve.compiler}
%define extends {Parser}
%define abstract
%define parser_class_name {VEParserBody}
%define throws {CompileException}
%define init_throws {CompileException}
%define lex_throws {CompileException}

%code imports {
import static ucar.ve.compiler.Types.*;
import static ucar.ve.compiler.VEParserBody.Lexer.*;
}

%code {
// Compiler actions
abstract void program(ActionList actions);
abstract Action action(Verb verb, ArgList args);
abstract Arg arg(int kind, String value);
abstract Verb verb(String name);
abstract ActionList actionlist(ActionList list, Action a);
abstract ArgList arglist(ArgList list, Arg arg);
}

%code {
    // Provide accessors for the parser lexer
    Lexer getLexer() {return this.yylexer;}
    void setLexer(Lexer lexer) {this.yylexer = lexer;}
}

%token <String> WORD STRING NUMBER
%token EOL

%type <Action> action
%type <Arg> arg
%type <Verb> verb
%type <ActionList> actionlist
%type <ArgList> arglist

%start program

%%

program:
	actionlist
		{program($1);}
	;

actionlist:
	  %empty
		{$$=actionlist(null,null);}
	| actionlist action
		{$$=actionlist($1,$2);}
	;

action:
	  verb arglist eol
		{$$=action($1,$2);}
	| '.' verb arglist eol
		{$$=action($2,$3);}
	;

verb:
	WORD
		{$$=verb($1);}

arglist:
	  %empty
		{$$=arglist(null,null);}
	| arglist arg
		{$$=arglist($1,$2);}
	;

arg:
	  WORD
		{$$=arg(WORD,$1);}
	| STRING
		{$$=arg(STRING,$1);}
	| NUMBER
		{$$=arg(NUMBER,$1);}
	;

eol:
	  EOL
	| ';'
	;
