FUNCTION zzsj06
PARAMETERS tcRyfl,tcCjgzsj,tcXlbm

DO case
CASE ALLTRIM(tcRyfl)="机关技术工人"
*!*	    IF tcXlbm>"61"
*!*	        RETURN STR(VAL(LEFT(tcCjgzsj,4))+2,4)+SUBSTR(tcCjgzsj,5,3)
*!*	    ELSE
        RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)
*!*	    ENDIF    

CASE ALLTRIM(tcRyfl)="事业技术工人"
*!*	    IF tcXlbm>"61"
*!*	        RETURN STR(VAL(LEFT(tcCjgzsj,4))+2,4)+SUBSTR(tcCjgzsj,5,3)
*!*	    ELSE
        RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)
*!*	    ENDIF    

OTHERWISE
    RETURN STR(VAL(LEFT(tcCjgzsj,4))+1,4)+SUBSTR(tcCjgzsj,5,3)

ENDCASE
