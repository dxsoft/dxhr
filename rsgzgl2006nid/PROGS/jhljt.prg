FUNCTION jhljt

PARAMETERS tcjhlqsny,tnzdjhlnx,tcrq,tcZwbm

*!*	LOCAL tcTbnd
*!*	tcTbnd=tbnd(tcrq,"bz06_jbt")

IF VAL(LEFT(tcrq,4))-VAL(LEFT(tcjhlqsny,4))>0 AND VAL(LEFT(tcjhlqsny,4))>0
*!*	    oldalias=ALIAS()
*!*	    SELECT bz06_jbt
*!*	    LOCATE FOR BETWEEN(VAL(LEFT(m.tcrq,4))-VAL(LEFT(tcjhlqsny,4)) - tnzdjhlnx,worklower,workupper) AND UPPER(item)="JHLJT" AND (zwbm=tczwbm OR EMPTY(zwbm)) AND tbnd=tcTbnd
*!*	    IF !EMPTY(oldalias)
*!*	        SELECT (oldalias)
*!*	    ENDIF
*!*	    IF FOUND("bz06_jbt")
*!*	        RETURN bz06_jbt.bz
*!*	    ENDIF

    LOCAL jhlnx
    jhlnx=VAL(LEFT(m.tcrq,4))-VAL(LEFT(tcjhlqsny,4)) - tnzdjhlnx
    DO CASE
    CASE BETWEEN(jhlnx,0,4)
        RETURN 000
    CASE BETWEEN(jhlnx,5,9)
        RETURN 3
    CASE BETWEEN(jhlnx,10,14)
        RETURN 5
    CASE BETWEEN(jhlnx,15,19)
        RETURN 7
    CASE BETWEEN(jhlnx,20,99)
        RETURN 10
    ENDCASE         
ENDIF
RETURN 000
