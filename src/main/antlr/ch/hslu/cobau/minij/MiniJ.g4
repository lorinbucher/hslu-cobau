grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

// parser rules
unit : (declaration | function | struct)* EOF;

// operators
multiplicative_ops: MUL | DIV | MOD;
additive_ops: ADD | SUB;
relational_ops: GREATER | LOWER | GEQUAL | LEQUAL;
equality_ops: EQUAL | NOTEQUAL;

// values
array_size: IDENTIFIER ACCESS SIZE;
boolean_value: TRUE | FALSE | variable | func_call | comp_expr | logic_expr;
numeric_value: NUMBER | array_size | variable | func_call | func_io_call | math_expr;
value: TEXT | variable | func_call | func_io_call | numeric_value | boolean_value;
type: type (LBRACK RBRACK) | (BOOLEAN | INTEGER | STRING | IDENTIFIER);

// structured data types
array_variable: IDENTIFIER (LBRACK numeric_value RBRACK)+;
struct_variable: IDENTIFIER ACCESS variable;
variable: IDENTIFIER | array_variable | struct_variable;

// function
func_builtin: READINT | WRITEINT | READCHAR | WRITECHAR;
func_call: IDENTIFIER func_call_arg_list;
func_call_arg_list: LPAREN (value (COMMA value)*)? RPAREN;
func_param: OUT? IDENTIFIER COLON type;
func_param_list: LPAREN (func_param (COMMA func_param)*)? RPAREN;
func_return_type: COLON type;
func_io_call: func_builtin LPAREN numeric_value? RPAREN;

// statements
assignment: variable ASSIGN (func_call | func_io_call | value | memory_expr);
declaration: IDENTIFIER COLON type SEMICOLON;
return_stmt: RETURN value?;
statement: (assignment | func_call | func_io_call | return_stmt) SEMICOLON;

// expressions
post_expr: variable (INC | DEC);
pre_expr: (INC | DEC) variable;
memory_expr: (INC INC | DEC DEC) variable; // NOTE: never heard of this, does that exist in any language?
comp_expr: LPAREN comp_expr RPAREN
    | math_expr relational_ops math_expr
    | (TEXT | math_expr) equality_ops (TEXT | math_expr);
logic_expr: LPAREN logic_expr RPAREN
    | NOT logic_expr
    | comp_expr
    | logic_expr AND logic_expr
    | logic_expr OR logic_expr
    | func_call
    | variable
    | TRUE
    | FALSE;
math_expr: LPAREN math_expr RPAREN
    | post_expr
    | pre_expr
    | (ADD | SUB) math_expr
    | math_expr multiplicative_ops math_expr
    | math_expr additive_ops math_expr
    | func_call
    | func_io_call
    | variable
    | array_size
    | NUMBER;

// (control) structures
block: LBRACE
    (block | condition | loop | statement)*
    RBRACE SEMICOLON?;
condition: IF LPAREN boolean_value RPAREN
    (block | statement)
    (ELSE IF LPAREN boolean_value RPAREN (block | statement))*
    (ELSE (block | statement))?;
function: FUNCTION IDENTIFIER func_param_list func_return_type? LBRACE
    declaration*
    (block | condition | loop | statement)*
    RBRACE SEMICOLON?;
loop: WHILE LPAREN boolean_value RPAREN (block | statement);
struct: STRUCT IDENTIFIER LBRACE declaration* RBRACE;


// scanner rules
BOOLEAN: 'boolean';
INTEGER: 'integer';
STRING: 'string';
FUNCTION: 'fun';

COMMA: ',';
COLON: ':';
SEMICOLON: ';';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LBRACK: '[';
RBRACK: ']';
ACCESS: '->';

ASSIGN: '=';
ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';
INC: '++';
DEC: '--';

NOT: '!';
EQUAL: '==';
NOTEQUAL: '!=';
GREATER: '>';
LOWER: '<';
GEQUAL: '>=';
LEQUAL: '<=';
AND: '&&';
OR: '||';
TRUE: 'true';
FALSE: 'false';

STRUCT: 'struct';
WHILE: 'while';
IF: 'if';
ELSE: 'else';
RETURN: 'return';

OUT: 'out';
SIZE: 'size';
READINT: 'readInt';
WRITEINT: 'writeInt';
READCHAR: 'readChar';
WRITECHAR: 'writeChar';

INDENT: [ \t\r\n]+ -> skip;
LINECOMMENT: '//' ~[\r\n]* -> skip ;
BLOCKCOMMENT: '/*' .*? '*/' -> skip;

NUMBER: [+-]?[0-9]+;
TEXT: '"' ('\\"'|.)*? '"';

IDENTIFIER: [a-zA-Z][a-zA-Z0-9_$]*;
