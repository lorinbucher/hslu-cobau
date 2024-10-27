grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

// Parser Rules
unit : (declaration | function | struct)* EOF;

built_in: (READINT | WRITEINT | READCHAR | WRITECHAR);
comparator: (EQUAL | NOTEQUAL | GREATER | LOWER | GEQUAL | LEQUAL);
logic_operator: (AND | OR);
math_operator: (ADD | SUB | MUL | DIV | MOD);
type: (BOOLEAN | INTEGER | STRING) | type (LBRACK RBRACK);

array_size: IDENTIFIER ACCESS SIZE;
boolean_value: (TRUE | FALSE);
numeric_value: (NUMBER | array_size);
value: (boolean_value | numeric_value | TEXT);

declaration: IDENTIFIER COLON (type | IDENTIFIER) SEMICOLON;
parameter: OUT? IDENTIFIER COLON (type | IDENTIFIER);
parameter_list: LPAREN (parameter (COMMA parameter)*)? RPAREN;
return_stmt: RETURN (value | expression)? SEMICOLON;

pre_expr: (INC | DEC) IDENTIFIER;
post_expr: IDENTIFIER (INC | DEC);
memory_expr: (INC INC | DEC DEC) IDENTIFIER; // NOTE: never heard of this, ask Martin Bättig

comp_expr: LPAREN? ((IDENTIFIER array_access? | NUMBER | struct_access) (comparator) (IDENTIFIER array_access? | NUMBER | struct_access) | IDENTIFIER array_access?) RPAREN?;
logic_expr: NOT? LPAREN? comp_expr (logic_operator NOT? comp_expr)* RPAREN?;
math_expr: (IDENTIFIER array_access? | NUMBER | pre_expr | post_expr | function_call | struct_access) math_operator (IDENTIFIER array_access? | NUMBER | pre_expr | post_expr | function_call | math_expr | struct_access)
    | SUB? LPAREN math_expr RPAREN
    | math_expr math_operator math_expr;
unary_expr: SUB? (IDENTIFIER | NUMBER)
    | NOT? IDENTIFIER
    | pre_expr
    | post_expr;
expression: comp_expr | logic_expr | math_expr | unary_expr;

assignment: (IDENTIFIER array_access? | struct_access) ASSIGN (value | expression | memory_expr | function_call | IDENTIFIER array_access?) SEMICOLON;
function_call: IDENTIFIER LPAREN ((value | expression) (COMMA (value | expression))*)? RPAREN;
io_call: built_in LPAREN (IDENTIFIER array_access? | NUMBER) RPAREN SEMICOLON;

block: LBRACE
    (assignment | block | condition | loop | io_call | function_call SEMICOLON)*
    return_stmt?
    RBRACE SEMICOLON?;
condition: IF LPAREN comp_expr RPAREN ((block
        (ELSE IF LPAREN comp_expr RPAREN block)*
        (ELSE block)?)
    | (return_stmt ELSE return_stmt));
function: FUNCTION IDENTIFIER parameter_list (COLON type)? LBRACE
    declaration*
    (assignment | block | condition | loop | io_call | function_call SEMICOLON)*
    return_stmt?
    RBRACE SEMICOLON?;
loop: WHILE LPAREN comp_expr RPAREN (block | assignment);
struct: STRUCT IDENTIFIER LBRACE declaration* RBRACE;
struct_access: IDENTIFIER (ACCESS IDENTIFIER array_access?)+;
array_access: (LBRACK (IDENTIFIER | NUMBER | expression) RBRACK)+;


// Scanner Rules
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
