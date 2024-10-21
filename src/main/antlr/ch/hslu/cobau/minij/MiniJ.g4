grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

unit : ; // empty rule to make project compile

program:  declaration+  (declaration* | function* | declaration statement*) EOF;
type: (INT | BOOLEAN | STRING | (INT | BOOLEAN | STRING) '[' ']');
declaration: IDENTIFIER COL type SEMI;

statement:  (assignment SEMI | expression SEMI | function | controlStructure);
assignment: IDENTIFIER EQUAL expression;
expression: postfix
           | prefix
           //| NUMBER
           | NUMBER dotExpr
           | NUMBER dashExpr
           | NUMBER relational
           | NUMBER equality;
postfix: IDENTIFIER (INCREMENT|DECREMENT);
prefix: (INCREMENT|DECREMENT|NOT|PLUS|MINUS)IDENTIFIER;
dotExpr: (TIMES | DIV | MOD) expression;
dashExpr: (PLUS | MINUS) expression;
relational:(GREATER | LOWER | GEQUAL | LEQUAL) expression;
equality: (EQUAL | NOTEQUAL);

function: FUN IDENTIFIER OPENB(/* TODO parameter definition, comma separated, 'name : type' */ )CLOSEB COL type OPENP
 (declaration* | expression*) RETURN (expression)? CLOSEP ;
controlStructure:  IF OPENB expression CLOSEB OPENP statement CLOSEP       // if (condition) {statement}
        (ELSE OPENP statement CLOSEP)?                     // 0 or 1 else statements
        |WHILE OPENB expression CLOSEB OPENP statement CLOSEP;             //(condition) {statement}

// TODO struct
// TODO array
// zuweisung ? zur Einschränkung der möglichen Zeichen, int können nur Zahlen sein, etc

BOOLEAN: 'boolean';
INT: 'int';
STRING: 'string';
FUN: 'fun'; // function => fun name(parameter1 : int, p2: boolean, TEXT: string){}

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


STRUCT: 'struct';
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
IDENTIFIER: [a-zA-Z][a-zA-Z0-9_]*;
INDENT: [ \t\r\n]+ -> skip;
MULTICOMMENT: '/*' .*? '*/' -> skip;
SINGLECOMMENT: '///' ~[\r\n]* -> skip;

