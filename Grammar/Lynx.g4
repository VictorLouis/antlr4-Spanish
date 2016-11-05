grammar Lynx;

programa : outmain* 'principal' comandos 'fin_principal' EOF ;
comandos : sentencias* ;
sentencias: declaracion | asignador | structstat | funcionSistema | funcionLeer | sentenciaIf | sentenciaWhile |sentenciaFor ;

outmain : funcionX | estructuras ;

funcionX : FUNCTION ID LEFTPAREN parametros RIGHTPAREN funcionBloque ;
parametros : (TYPES ID)* (COMMA TYPES ID)*;
funcionBloque : LEFTBRACE comandos RETURN expresion SEMICOLON RIGHTBRACE ;
parametrosCall : expresion* (COMMA expresion)* ;

estructuras : STRUCT ID LEFTBRACE declaracion+ RIGHTBRACE ;
decstructvar : STRUCT ID CREATE ID SEMICOLON; 
asignstruct : ID ARROW ID EQUALS expresion SEMICOLON ;
structstat : decstructvar | asignstruct ;

declaracion : TYPES ID SEMICOLON ;
asignador : TYPES ID EQUALS expresion SEMICOLON | ID EQUALS expresion SEMICOLON ;
funcionSistema : PRINT LEFTPAREN expresion RIGHTPAREN SEMICOLON ;
funcionLeer: SCAN LEFTPAREN ID COMMA TYPES RIGHTPAREN SEMICOLON ;

condiciones: LEFTPAREN expresion RIGHTPAREN sentenciasBloque ;
sentenciasBloque: LEFTBRACE comandos RIGHTBRACE | sentencias ;
sentenciaIf: IF condiciones (ELSEIF condiciones)* (ELSE sentenciasBloque)? ;
sentenciaWhile: WHILE LEFTPAREN expresion RIGHTPAREN sentenciasBloque ;
sentenciaFor: FOR LEFTPAREN asignador expresion SEMICOLON REAL RIGHTPAREN sentenciasBloque ;

arreglos: TYPES ID EQUALS SQRLBRAQUET atomic* (',' atomic)* SQRRBRAQUET SEMICOLON ;

expresion
 : MINUS expresion                           										#unaryMinusExpr
 | NOT expresion                            										#notExpr
 | expresion op=(MULT | DIV | MOD) expresion      									#multiplicationExpr
 | expresion op=(PLUS | MINUS) expresion          									#additiveExpr
 | expresion op=(LTEQ | GTEQ | LT | GT) expresion 									#relationalExpr
 | expresion op=(EQUALITY | NOTEQUAL) expresion   									#equalityExpr
 | opt=(SEN | COS | TAN | SENH | COSH | TANH) LEFTPAREN expresion RIGHTPAREN  		#trigExpr
 | opt=(MIN | MAX) LEFTPAREN expresion COMMA expresion RIGHTPAREN  					#twoparamExpr
 | opt=(CEIL | FLOOR | SQRT | ABS | LOG | LOG10) LEFTPAREN expresion RIGHTPAREN  	#mathfuncExpr
 | ID LEFTPAREN parametrosCall RIGHTPAREN											#callfuncExpr
 | expresion AND expresion                        									#andExpr
 | expresion OR expresion                         									#orExpr
 | <assoc=right> expresion POW expresion											#powExpr
 | ID PERIOD ID																		#structExpr
 | atomic                                 											#atomExpr
 ;
 
atomic 
 :	LEFTPAREN expresion RIGHTPAREN  #parExpr
 |	INT 							#integerAtom
 |  REAL  							#floatAtom
 |	BOOLEAN 						#booleanAtom
 |	ID             					#idAtom
 |	CHAR							#charAtom
 |	STRING         					#stringAtom
 | 	NULL							#nullAtom
 ;

COMMENT 		: '/*' .*? '*/' -> skip ;
LINE_COMMENT 	: '//' ~[\r\n]* -> skip ;

TYPES 			: 'booleano' | 'entero' | 'real' | 'cadena' | 'caracter' ; 
BREAK			: 'romper' ; 
NULL			: 'nulo' ;
REAL 			: [0-9]*[.][0-9]+ ;
BOOLEAN			: 'verdadero' | 'falso' ;
INT 			: [0-9]+ ; 
STRING 			: '"' (~["\r\n] | '""')* '"' ;
CHAR 			: [']~[']['] ;
PLUS			: '+' ;
MINUS			: '-' ;
MULT			: '*' ;
DIV 			: '/' ;
MOD 			: '%' ;
POW 			: '^' ;
GT 				: '>' ;
GTEQ 			: '>=' ;
LT 				: '<' ;
LTEQ 			: '<=' ;
EQUALITY	 	: '==' ;
NOTEQUAL		: '!=' ;
EQUALS 			: '=' ;
ARROW 			: '->' ;
COLON			: ':' ;
SEMICOLON		: ';' ;
LEFTPAREN 		: '(' ;
RIGHTPAREN 		: ')' ;
LEFTBRACE 		: '{' ;
RIGHTBRACE 		: '}' ;
SQRLBRAQUET		: '[' ;
SQRRBRAQUET		: ']' ;
PERIOD			: '.' ;
OR 				: '||' ;
AND 			: '&&' ;
NOT				: '!' ;
COMMA 			: ',' ;
STRUCT			: 'estructura' ;
CREATE			: 'crear' ;
PRINT			: 'pintela';
SCAN			: 'leer';
IF				: 'si';
THEN			: 'entonces';
ELSEIF			: 'sino_si' ;
ELSE			: 'si_no';
FOR				: 'para' ;
WHILE			: 'mientras' ;
DO				: 'hacer' ;
FUNCTION		: 'funcion' ;
RETURN			: 'devuelve';
SEN             : 'seno' ;
COS             : 'cosen' ;
TAN             : 'tangente' ;
SENH            : 'senoh' ;
COSH            : 'cosenoh' ;
TANH            : 'tangenteh' ;
MIN				: 'min' ;
MAX				: 'max' ;
CEIL			: 'techo' ;
FLOOR			: 'piso' ;
SQRT			: 'raiz2' ;
ABS				: 'abs' ;
LOG				: 'log' ;
LOG10			: 'log10' ;
ID 				: [a-zA-Z][a-zA-Z0-9_]* ;

WS
:
	[ \t\r\n]+ -> skip
;