%{
import java.io.*;
%}

%token <ival> INUM
%token <dval> DNUM
%token <sval> CH
%token <sval> ID
%token <sval> TRUE
%token <sval> FALSE
%token BOOL
%token CHAR
%token INT
%token FLOAT
%token CONST
%token IF
%token ELSE
%token FOR
%token REPEAT
%token UNTIL
%token PROCEDURE
%token RETURN
%token BREAK
%token CASE
%token CONTINUE
%token DEFAULT
%token DOUBLE
%token EXTERN
%token FUNCTION
%token FOREACH
%token GOTO
%token INPUT
%token IN
%token LONG
%token OUTPUT
%token OF
%token RECORD
%token SIZEOF
%token STATIC
%token STRING
%token SWITCH
%token EQ
%token NEQ
%token LEQ
%token BEQ
%token LAND
%token LOR
%token INC
%token DEC
%token VOID
%token AUTO
%type <sval> store_pc
%type <sval> exp
%type <sval> else_jp
%type <sval> else_part
%type <sval> dcl_assign;
%type <sval> variable;
%type <sval> reserve_line;
%type <sval> method_call;
%type <sval> foreach_resv_line;
%left LOR
%left LAND
%left EQ NEQ LEQ BEQ '<' '>'
%left '-' '+'
%left '*' '/'
%left '%'
%left '|'
%left '&'
%left '^'
%nonassoc INC DEC '~' '!'
%nonassoc UMINUS
%start program

%%

program:
    prog_list {cg.writeOutput();}
    ;

prog_list:
    var_dcl prog_list
    | func_proc prog_list
    | struct_dec prog_list
    | var_dcl
    | func_proc
    | struct_dec
    ;

struct_dec:
    RECORD ID '{' struct_var_dcl '}' ';'
    ;

struct_var_dcl:
    var_dcl
    | struct_var_dcl var_dcl
    ;

func_proc:
    proc_dcl
    | func_dcl
    | extern_dcl
    ;

extern_dcl:
    EXTERN type ID ';'


func_dcl:
    type ID reserve_line  open_scp '(' init_argms arguments ')'  block del_scp {cg.func($2, $3);}
    | type ID reserve_line  open_scp '(' init_argms arguments ')' ';' del_scp{cg.func($2, $3);}
    ;

proc_dcl:
    PROCEDURE null_type_var_dcl ID reserve_line  open_scp '(' init_argms arguments ')'  block del_scp{cg.proc($3, $4);}
    | PROCEDURE null_type_var_dcl ID reserve_line  open_scp '(' init_argms arguments ')'  ';' del_scp{cg.proc($3, $4);}
    ;

null_type_var_dcl:
    {cg.typeVarDcl = "";}
    ;

init_argms:
     {cg.initArgms();}
     ;

reserve_line:
    {$$ = cg.resvLine();}
    ;

arguments:
    arguments_chain
    |
    ;

arguments_chain:
    arguments_chain ',' type ID arg_brackets{cg.arg($4);}
    | type ID arg_brackets{cg.arg($2);}
    ;

arg_brackets:
    '['']' arg_brackets
    |
    ;

block:
    '{' block_list '}'
    ;

block_list:
     stm block_list
    | var_dcl block_list
    |
    ;

var_dcl:
    CONST type const_dcl_first const_dcl_chain ';' {}
    | type var_dcl_first var_dcl_chain ';' {}
    ;

const_dcl_chain:
    ',' const_dcl_first const_dcl_chain {}
    |
    ;

const_dcl_first:
    variable '=' INUM{cg.constVarDcl($1, $3);}
    ;

var_dcl_chain:
    ',' var_dcl_first var_dcl_chain {}
    |
    ;

var_dcl_first:
    variable dcl_assign {cg.varDcl($1, $2);}
    ;

dcl_assign:
    '=' exp {$$ = $2;}
    | {$$ = null;}
    ;

variable:
    ID init array struct_field {$$ = $1;}
    | INC variable {$$ = cg.inc($2);}
    | DEC variable {$$ = cg.dec($2);}
    | '~' variable {$$ = cg.bitWiseNeg($2);}
    ;

struct_field:
    '.' variable
    |
    ;

init:
    {cg.arrayInit();}
    ;

array:
    '[' exp ']' array {cg.addArrayDim(Integer.parseInt($2.substring(1)));}
    |
    ;

type:
    BOOL {cg.typeVarDcl = "BOOL";}
    | CHAR {cg.typeVarDcl = "CHAR";}
    | INT {cg.typeVarDcl = "INT";}
    | FLOAT {cg.typeVarDcl = "FLOAT";}
    | LONG
    | DOUBLE
    | ID
    | STRING
    | VOID
    | AUTO
    ;

stm:
    assign ';' {}
    | cond_stm
    | loop_stm
    | RETURN return_value ';'
    | method_call ';'
    | BREAK ';'
    | CONTINUE ';'
    | SIZEOF '(' type ')' ';'
    | goto ';'
    | lable
    ;

goto:
    GOTO ID
    ;

lable:
    ID ':'
    ;

method_call:
    ID '(' init_parm parametres ')' {$$ = cg.methodCall($1);}
    ;

init_parm:
    {cg.initParms();}
    ;

parametres:
    parametres_chain
    |
    ;

parametres_chain:
    parametres_chain ',' exp {cg.addParms($3);}
    | exp {cg.addParms($1);}
    ;

return_value:
    exp {cg.returnFP($1);}
    | {cg.returnFP(null);}
    ;

loop_stm:
    FOR open_scp '(' for_var_dcl ';' exp store_pc if_jz ';' for_assg_exp ')' block del_scp {cg.completeFor($6, $7);}
    | REPEAT store_pc open_scp block del_scp UNTIL '(' exp ')' {cg.completeRepeat($8, $2);}
    | FOREACH open_scp foreach_resv_line '(' ID IN ID ')' block del_scp {cg.foreach($3,  $5, $7);}
    ;

foreach_resv_line:
    {cg.pc += 5; $$ = "" + (cg.pc - 5);}
    ;

for_var_dcl:
    var_dcl
    |
    ;

for_assg_exp:
    assign
    | exp
    |
    ;

cond_stm:
    IF '(' exp ')' store_pc if_jz open_scp block del_scp else_part {cg.completeIf($3, $5, $10);}
    | SWITCH '(' ID ')' OF ':' '{' case_chain DEFAULT ':' block '}'
    ;

case_chain:
    case_chain CASE INUM ':' block
    |
    ;

else_part:
    ELSE else_jp store_pc open_scp block del_scp {cg.elsePart($2); $$ = $3;}
    | store_pc {$$ = $1;}
    ;

else_jp:
    {$$ = cg.elseJP();}
    ;

open_scp:
    {cg.openScope();}
    ;

del_scp:
    {cg.deleteScope();}
    ;

if_jz:
    {cg.ifJZ();}
    ;

store_pc:
    {$$ = cg.storePC();}
    ;

assign:
    variable '=' exp {cg.assign($1,$3);}
    ;

exp:
    INUM {cg.addIntTable($1); $$ = "#" + String.valueOf($1);}
    | DNUM {cg.addFloatTable($1); $$ = "#" + String.valueOf($1);}
    | TRUE {$$ = $1;}
    | FALSE {$$ = $1;}
    | CH {cg.addCharTable($1); $$ = $1;}
    | variable {$$ = $1;}
    | '('exp')' {$$ = $2;}
    | method_call {cg.checkHasValue($1); $$ = $1;}
    | '-' exp %prec UMINUS {$$ = cg.arithmeticOperand("-",$2);}
    | '!' exp {$$ = cg.arithmeticOperand("!",$2);}
    | exp '+' exp {$$ = cg.arithmeticOperand("+",$1,$3);}
    | exp '-' exp {$$ = cg.arithmeticOperand("-",$1,$3);}
    | exp '*' exp {$$ = cg.arithmeticOperand("*",$1,$3);}
    | exp '/' exp {$$ = cg.arithmeticOperand("/",$1,$3);}
    | exp '%' exp {$$ = cg.arithmeticOperand("%",$1,$3);}
    | exp '^' exp {$$ = cg.bitWiseOperand("^",$1,$3);}
    | exp '|' exp {$$ = cg.bitWiseOperand("|",$1,$3);}
    | exp '&' exp {$$ = cg.bitWiseOperand("&",$1,$3);}
    | exp LAND exp {$$ = cg.logicalOperand("&&",$1,$3);}
    | exp LOR exp {$$ = cg.logicalOperand("||",$1,$3);}
    | exp EQ exp {$$ = cg.conditionalOperand("==",$1,$3);}
    | exp NEQ exp {$$ = cg.conditionalOperand("!=",$1,$3);}
    | exp LEQ exp {$$ = cg.conditionalOperand("<=",$1,$3);}
    | exp BEQ exp {$$ = cg.conditionalOperand(">=",$1,$3);}
    | exp '>' exp {$$ = cg.conditionalOperand(">",$1,$3);}
    | exp '<' exp {$$ = cg.conditionalOperand("<",$1,$3);}
    ;

%%

/* a reference to the lexer object */
private Yylex lexer;
CG cg;
/* interface to the lexer */
private int yylex () 
{
	int yyl_return = -1;
	try
	{
		yyl_return = lexer.yylex();
	}
	catch (IOException e) 
	{
		System.err.println("IO error :"+e);
	}
	return yyl_return;
}
/* error reporting */
public void yyerror (String error) 
{
	System.err.println ("Error: " + error);
}
/* lexer is created in the constructor */
public Parser(Reader r) 
{
    cg= new CG();
	lexer = new Yylex(r, this);
}
/* that's how you use the parser */
public static void main(String args[]) throws IOException 
{
	Parser yyparser = new Parser(new FileReader("cCode.txt"));
	yyparser.yyparse();
}
