grammar TagQuery;

//lexer
tagquery
  : object EOF
  ;

object
  : tagexp
  | '(' object ')'
  | object logical_operator object
  ;

tagexp
  : key
  | NOT key
  | key boolean_operator value
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

array_operator
  : IN
  | NOT IN
  ;

array
  : '[' value (',' value)* ']'
  | '[' ']' // empty array
  ;

value
 : SIMPLETEXT
 | COMPLEXTEXT
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
IN: I N;

SIMPLETEXT  : [a-zA-Z_0-9.][\-a-zA-Z_0-9.]* ;
COMPLEXTEXT :  '\'' (ESC | ~['\\])* '\'' ;

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

fragment NEG_OP: '~';