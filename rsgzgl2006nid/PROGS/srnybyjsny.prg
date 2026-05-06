FUNCTION srnybyjsny

PARAMETERS mjsnf,mjsyf

IF mjsyf="01"
    RETURN STR(VAL(mjsnf)-1,4)+".12"
ELSE
    RETURN mjsnf+"."+PADL(ALLTRIM(STR(VAL(mjsyf)-1)),2,'0')
ENDIF