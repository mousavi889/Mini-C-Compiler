%%
%byaccj
%{
/* store a reference to the parser object */
private Parser yyparser;
/* constructor taking an additional parser object */
public Yylex(java.io.Reader r, Parser yyparser) {
this(r);
this.yyparser = yyparser;
}
%}

INUM = 0 | [1-9][0-9]* | "0x" [0-9]+
DNUM = [0-9]+ "." [0-9]* | [0-9]* "." [0-9]+ | [0-9]+ ["e" | "E"] ["+" | "-"] [0-9]
CH = "'" [^WS] "'"
ID = [:jletter:] [:jletterdigit:]*
LINETERM = \r|\n|\r\n
INPUTCHAR = [^\r\n]
WS = {LINETERM} | [ \t\f]
COMMENT = {TRADCM} | {ENDLCM} | {DOCCM}
TRADCM = "/#" [^#] ~"#/" | "/#" "#"+ "/"
ENDLCM = "##" {INPUTCHAR}* {LINETERM}?
DOCCM = "/##" {CMCON} "#"+ "/"
CMCON = ( [^#] | \#+ [^/#] )*

%%

"auto" {return Parser.AUTO;}
"break" {return Parser.BREAK;}
"foreach" {return Parser.FOREACH;}
"in" {return Parser.IN;}
"true" {yyparser.yylval = new ParserVal(yytext()); return Parser.TRUE;}
"false" {yyparser.yylval = new ParserVal(yytext()); return Parser.FALSE;}
"bool" {return Parser.BOOL;}
"char" {return Parser.CHAR;}
"int" {return Parser.INT;}
"float" {return Parser.FLOAT;}
"const" {return Parser.CONST;}
"if" {return Parser.IF;}
"else" {return Parser.ELSE;}
"for" {return Parser.FOR;}
"repeat" {return Parser.REPEAT;}
"until" {return Parser.UNTIL;}
"procedure" {return Parser.PROCEDURE;}
"return" {return Parser.RETURN;}
"void" {return Parser.VOID;}
"case"  {return Parser.CASE;}
"continue" {return Parser.CONTINUE;}
"default" {return Parser.DEFAULT;}
"double" {return Parser.DOUBLE;}
"extern" {return Parser.EXTERN;}
"function" {return Parser.FUNCTION;}
"goto" {return Parser.GOTO;}
"input" {return Parser.INPUT;}
"long" {return Parser.LONG;}
"output" {return Parser.OUTPUT;}
"of" {return Parser.OF;}
"record" {return Parser.RECORD;}
"sizeof" {return Parser.SIZEOF;}
"static" {return Parser.STATIC;}
"string" {return Parser.STRING;}
"switch" {return Parser.SWITCH;}
"." |
"," |
":" |
";" |
"<" |
">" |
"=" |
"!" |
"~" |
"&" |
"|" |
"^" |
"*" |
"+" |
"-" |
"/" |
"%" |
"[" |
"]" |
"{" |
"}" |
"(" |
")" {return (int) yycharat(0);}
"++" {return Parser.INC;}
"--" {return Parser.DEC;}
"==" {return Parser.EQ;}
"!=" {return Parser.NEQ;}
"<=" {return Parser.LEQ;}
">=" {return Parser.BEQ;}
"&&" {return Parser.LAND;}
"||" {return Parser.LOR;}
{COMMENT} {}
{WS} {}
{INUM} {yyparser.yylval = new ParserVal(Integer.parseInt(yytext())); return Parser.INUM;}
{DNUM} {yyparser.yylval = new ParserVal(Double.parseDouble(yytext())); return Parser.DNUM;}
{CH} {yyparser.yylval = new ParserVal(yytext()); return Parser.CH;}
{ID} {yyparser.yylval = new ParserVal(yytext()); return Parser.ID; }
. {}
<<EOF>> {return 0;}
