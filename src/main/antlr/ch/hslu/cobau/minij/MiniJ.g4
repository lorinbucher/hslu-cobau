grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

unit : ; // empty rule to make project compile

tokens: declaration | (declaration* | function* |statement*) SEMI EOF;
statement: INDENT | /*ASSIGNMENT*/ (expression | INDENT | block) SEMI;
expression: postfix
           | prefix
           | NUMBER dotExpr
           | NUMBER dashExpr
           | NUMBER relational
           | NUMBER equality
           | NUMBER;
type: (INT|BOOLEAN|STRING);
declaration: identifier COL type SEMI;
postfix: identifier(INCREMENT|DECREMENT);
prefix: (INCREMENT|DECREMENT|NOT|PLUS|MINUS)identifier;
dotExpr: (TIMES | DIV | MOD) expression;
dashExpr: (PLUS | MINUS) expression;
relational:(GREATER | LOWER | GEQUAL | LEQUAL) expression;
equality: (EQUAL | NOTEQUAL);

function: FUN identifier OPENB(/* TODO parameter definition, comma separated, 'name : type' */ )CLOSEB COL type OPENP
 (declaration* | expression*) (RETURN expression)? CLOSEP ;
block:  IF OPENB expression CLOSEB OPENP statement CLOSEP       // if (condition) {statement}
        (ELSE OPENP statement CLOSEP)?                     // 0 or 1 else statements
        |WHILE OPENB expression CLOSEB OPENP statement CLOSEP;             //(condition) {statement}
identifier: (LETTERS+(LETTERS | DIGITS)*);
// TODO struct
// TODO array


DOT: '.';
COL: ':';
SEMI: ';';
OPENB: '(';
CLOSEB:')';
OPENP:'{';
CLOSEP:'}';

PLUS: 'plus'|'+';
MINUS: 'minus'|'-';
TIMES: 'times'|'*';
DIV: 'div'|'/';
MOD: 'mod' | '%';
INCREMENT: '++';
DECREMENT: '--';

NOT: '!';
EQUAL: '==';
NOTEQUAL: '!=';
GREATER: '>';
LOWER: '<';
GEQUAL: '>=';
LEQUAL: '<=';
LAND: '&&';
LOR: '||';
TRUE: 'true';
FALSE: 'false';

BOOLEAN: 'boolean';
INT: 'int'|'integer';
STRING: 'string';
STRUCT: 'struct';
FUN: 'fun'|'function'; // function => fun name(parameter1 : int, p2: boolean, TEXT: string){}
IF: 'if';
ELSE: 'else';
WHILE: 'while';
OUT: 'out';
SIZE: 'size';
RETURN: 'return';
READINT: 'readInt';
WRITEINT: 'writeInt';
READCHAR: 'readChar';
WRITECHAR: 'writeChar';
DIGITS: [0-9];
NUMBER: '-'?DIGITS+;
LETTERS: [a-zA-Z]+;
INDENT: [ \t\r\n]+ -> skip;
MULTICOMMENT: '/*' .*? '*/' -> skip;
SINGLECOMMENT: '///' ~[\r\n]* -> skip;

