/**
 * Reference grammar for language "MiniJ HS24"
 *
 * Copyright (c) 2020-2024 HSLU Informatik. All rights reserved.
 * This code and any derivative work thereof must remain private.
 * Public distribution is prohibited.
 */
grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

///////////////////////////////////////////////////////////////////////////////
// Parsing rules
///////////////////////////////////////////////////////////////////////////////

// declaractions
unit        : member* EOF;
member      : declaration | struct | function | SEMICOLON;

struct      : STRUCT identifier LBRACE (declaration)* RBRACE;

// procedures and blocks
function      : FUNCTION identifier LPAREN (parameter (COMMA parameter)*)?  RPAREN (COLON type)? declarations functionBody;
parameter     : (REF)? identifier COLON type;
declarations  : (declarationStatement)*;

functionBody  : LBRACE (declarationStatement)* (statement)* RBRACE;
block         : LBRACE (statement)* RBRACE;

// statements
declarationStatement : declaration | SEMICOLON;
statement            : assignment | callStatement | returnStatement | ifStatement | whileStatement | block | SEMICOLON;

assignment           : memoryAccess ASSIGN expression SEMICOLON;
callStatement        : call SEMICOLON;
whileStatement       : WHILE LPAREN expression RPAREN statement;
ifStatement          : IF LPAREN expression RPAREN statement (elseClause)?;
elseClause           : ELSE statement;
returnStatement      : RETURN (expression)? SEMICOLON;

// expressions
// NOTE: The order of the following subrules is important. In ANTLR order reflects the associativity
//       of the operations. Thus, operator with highest precendence MUST be listed first.
expression : LPAREN expression RPAREN
           | memoryAccess (INCREMENT | DECREMENT)
           | unaryExpression
           | expression binaryOp=(TIMES | DIV | MOD) expression
           | expression binaryOp=(PLUS | MINUS) expression
           | expression binaryOp=(LESSER | GREATER | LESSER_EQ | GREATER_EQ) expression
           | expression binaryOp=(EQUAL | UNEQUAL) expression
           | expression binaryOp=AND expression
           | expression binaryOp=OR expression
           | call
           | trueConstant
           | falseConstant
           | integerConstant
           | stringConstant
           | memoryAccess
           ;

call       : identifier LPAREN (expression (COMMA expression)*)? RPAREN;

unaryExpression : unaryOp=(NOT | MINUS | PLUS | INCREMENT | DECREMENT) expression;
trueConstant    : TRUE;
falseConstant   : FALSE;
integerConstant : INTEGER;
stringConstant  : STRINGCONSTANT;
memoryAccess    : ID
                | memoryAccess ARROW SIZE
                | memoryAccess ARROW ID
                | memoryAccess LBRACKET expression RBRACKET
                ;

// types and identifier
declaration   : identifier COLON type SEMICOLON;
type          : basicType | type LBRACKET RBRACKET;
basicType     : integerType | booleanType | stringType | structType;
integerType   : INT;
stringType    : STRING;
booleanType   : BOOLEAN;
structType    : identifier;

identifier    : ID;

///////////////////////////////////////////////////////////////////////////////
// Lexer rules
///////////////////////////////////////////////////////////////////////////////

// operators, blocks, arrays indexes, and parameter lists
LPAREN:        '(';
RPAREN:        ')';
LBRACE:        '{';
RBRACE:        '}';
LBRACKET:      '[';
RBRACKET:      ']';
COLON:         ':';
SEMICOLON:     ';';
COMMA:         ',';
ASSIGN:        '=';
INCREMENT:     '++';
DECREMENT:     '--';
PLUS:          '+';
MINUS:         '-';
TIMES:         '*';
DIV:           '/';
MOD:           '%';
ARROW:         '->';
EQUAL:         '==';
UNEQUAL:       '!=';
LESSER:        '<';
GREATER:       '>';
LESSER_EQ:     '<=';
GREATER_EQ:    '>=';
NOT:           '!';
AND:           '&&';
OR:            '||';


// declaraction
STRUCT:        'struct';
FUNCTION:      'fun';
REF:           'out';

// control flow
IF:            'if';
ELSE:          'else';
WHILE:         'while';
RETURN:        'return';

// types
INT:           'integer';
BOOLEAN:       'boolean';
STRING:        'string';

// special
SIZE:          'size';

// values
TRUE:          'true';
FALSE:         'false';
INTEGER:        ('+'|'-')?[0-9]+;
STRINGCONSTANT: '"' (~'"')* '"'; //

// identifiers: order is important as all other keywords have precendence
ID : [a-zA-Z][a-zA-Z0-9_$]*;

// comments
LINE_COMMENT: '//' ~[\r\n]* -> skip; // skip contents of line comments
BLOCKCOMMENT: '/*' .*? '*/' -> skip; // skip contents of block comments
WS:           [ \t\r\n]+    -> skip; // skip spaces, tabs, newlines
