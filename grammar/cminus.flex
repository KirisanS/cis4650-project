// --------------------------Usercode Section------------------------
   
import java_cup.runtime.*;
      
%%
   
// -----------------Options and Declarations Section-----------------
// The name of the class JFlex will create will be Lexer.
// Will write the code to the file Lexer.java. 
%class Lexer

%eofval{
  return symbol(sym.EOF);
%eofval};

// The current line number can be accessed with the variable yyline
// and the current column number with the variable yycolumn.
%line
%column
    
// Will switch to a CUP compatibility mode to interface with a CUP
// generated parser.
%cup

%x COMMENT
   
// Declarations

// Code between %{and %}, both of which must be at the beginning of a
// line, will be copied letter to letter into the lexer class source.
// Here you declare member variables and functions that are used inside
// scanner actions.  
%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
   }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
   }

    // error handling
    private void lexicalError(String text) {
        System.err.println(
            "Lexical error at line " + (yyline + 1) +
            ", column " + (yycolumn + 1) +
            ": illegal character '" + text + "'"
        );
   }
%}

LETTER = [a-zA-Z_]
DIGIT = [0-9]
ID = {LETTER}({LETTER}|{DIGIT})*
NUM = {DIGIT}+
WHITESPACE  = [ \t\r\n]+

%%
// reserved words
"bool"      {return symbol(sym.BOOL);}
"else"      {return symbol(sym.ELSE);}
"if"        {return symbol(sym.IF);}
"int"       {return symbol(sym.INT);}
"return"    {return symbol(sym.RETURN);}
"void"      {return symbol(sym.VOID);}
"while"     {return symbol(sym.WHILE);}
"true"      {return symbol(sym.TRUTH, true);}
"false"     {return symbol(sym.TRUTH, false);}

// operators
"<="        {return symbol(sym.LESSEQUAL);}
">="        {return symbol(sym.GREATEQUAL);}
"=="        {return symbol(sym.EQ);}
"!="        {return symbol(sym.NOTEQUAL);}
"||"        {return symbol(sym.OR);}
"&&"        {return symbol(sym.AND);}
"<"         {return symbol(sym.LESSTHAN);}
">"         {return symbol(sym.GREATERTHAN);}
"+"         {return symbol(sym.PLUS);}
"-"         {return symbol(sym.MINUS);}
"*"         {return symbol(sym.TIMES);}

// JFlex matches the longest possible rule
// but it also prioritizes rule order for equal-length matches
// so that's why comment rule  is above the single / rule
"/*"                {yybegin(COMMENT);}

<COMMENT>"*/"       {yybegin(YYINITIAL);}

<COMMENT>([^]|\n) {/* skip everything inside comment */}

"/"         {return symbol(sym.DIVIDE);}
"~"         {return symbol(sym.NOT);}
"="         {return symbol(sym.ASSIGN);}
";"         {return symbol(sym.SEMICOLON);}
","         {return symbol(sym.COMMA);}
"("         {return symbol(sym.LPAREN);}
")"         {return symbol(sym.RPAREN);}
"["         {return symbol(sym.LBRACKET);}
"]"         {return symbol(sym.RBRACKET);}
"{"         {return symbol(sym.LBRACE);}
"}"         {return symbol(sym.RBRACE);}

// misc
{ID}        {return symbol(sym.ID, yytext());}
{NUM}       {return symbol(sym.NUM, Integer.parseInt(yytext()));}
{WHITESPACE} {/* skip whitespace */}   
.           {lexicalError(yytext());}