grammar Expression;

//lexer
expression
  : object EOF
  ;

object
  : expr
  | '(' object ')'
  | object logical_operator object
  ;

expr
  : key
  | NOT key
  | key boolean_operator value
  | key compare_operator numerical_value
  | key array_operator array
  ;

logical_operator
  : AND
  | OR
  ;

boolean_operator
  : EQUAL
  | NOTEQUAL
  ;

compare_operator
  : GT
  | GTE
  | LT
  | LTE
  ;

array_operator
  : IN
  | NOT IN
  ;

array
  : '[' value (',' value)* ']'
  | '[' ']' // empty array
  ;

value
 : numerical_value
 | SIMPLETEXT
 | STRING
 ;

numerical_value
  : INTEGER
  | FLOAT
  ;

key
 : SIMPLETEXT
 ;

//parser
OR: O R;
AND: A N D;
NOT: N O T;
EQUAL: '=';
NOTEQUAL: '!=';

// Allow only for numbers
GT : '>';
GTE : '>=';
LT : '<';
LTE : '<=';

// Allow only for array?
IN: I N;

// Needs maybe INTEGER OR FLOAT? Use fragments?
//NUMBER : [0-9.]+ ;

FLOAT : INTEGER '.' INTEGER ;
INTEGER : [0-9]+ ;

SIMPLETEXT  : [a-zA-Z_0-9.][\-a-zA-Z_0-9.]* ;
STRING :  '\'' ( ESC | ~('\\'|'\'') )* '\''
          |'"' ( ESC | ~('\\'|'"') )* '"';
//COMPLEXTEXT :  '\'' (ESC | ~['\\])* '\'' ;

WS  :   [ \t\n\r]+ -> skip ;


//fragments
fragment ESC : '\\' (['\\/bfnrt] | UNICODE | NEG_OP) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

fragment NEG_OP: '!';