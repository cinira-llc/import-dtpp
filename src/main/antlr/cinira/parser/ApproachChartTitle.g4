grammar ApproachChartTitle;

@header {
    package cinira.parser;
}

name : title (COMMA 'CONT.' continuation=DIGIT)? ;

title
    : visual
    | copter
    | instrument
    ;

visual : landmark 'VISUAL' runways ;

landmark : LETTERS (LETTERS | RUNWAY | HEADING | NUMBER)* ;

copter : 'COPTER' approaches (runways | heading=HEADING) ;

instrument : hi='HI'? approaches (runways | circle=LETTER) suffix? ;

approaches : approach (OR approach)* ;

approach : guidance variant=LETTER? ;

suffix
    : LPAREN converging='CONVERGING' RPAREN
    | LPAREN sa='SA'? 'CAT' (categories+=LETTER | categories+=LETTERS)+ RPAREN
    ;

guidance
    : type=GLS
    | type=GPS
    | converging='CONVERGING'? type=ILS (SLASH equipment=DME)? prm=PRM?
    | type=LDA (SLASH equipment=DME)?
    | type=LOC (SLASH equipment=(DME | NDB))? bc='BC'?
    | type=NDB (SLASH equipment=DME)?
    | type=RNAV LPAREN equipment=GPS RPAREN prm=PRM?
    | type=RNAV LPAREN equipment=RNP RPAREN
    | type=SDF
    | type=TACAN
    | type=VOR (SLASH equipment=DME)? differentiator=DIGIT?
    ;

runways : 'RWY' number=RUNWAY positions+=LETTER? (SLASH positions+=LETTER)* ;

OR : 'OR' ;

DME : 'DME' ;

GLS : 'GLS' ;

GPS : 'GPS' ;

ILS : 'ILS' ;

LDA : 'LDA' ;

LOC : 'LOC' ;

NDB : 'NDB' ;

PRM : 'PRM' ;

RNAV : 'RNAV' ;

RNP : 'RNP' ;

SDF : 'SDF' ;

TACAN : 'TACAN' ;

VOR : 'VOR' ;

DIGIT : [0-9] ;

/* Runway number, zero padded to two digits: 01-36. */
RUNWAY
    : '0' [1-9]
    | [1-2] DIGIT
    | '3' [0-6]
    ;

/* Heading, zero padded to three digits: 000-360.  */
HEADING
    : '360'
    | [0-2] DIGIT DIGIT
    | '3' [0-5] DIGIT
    ;

/* Any (non-zero padded) number not matching RUNWAY or HEADING. */
NUMBER
    : '0'
    | [1-9] DIGIT *
    ;

LETTER : [A-Z] ;

LETTERS : LETTER + ;

COMMA : ',' ;

LPAREN : '(' ;

RPAREN : ')' ;

SLASH : '/' ;

NL : '\r'? '\n' ;

WS : [ \\-] + -> skip ;
