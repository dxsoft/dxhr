FUNCTION qtqk

PARAMETERS tcdwbm,tcgrbm,tcjslb,tcjsnf,tcjsyf

*!*	LOCAL talias
*!*	talias=ALIAS()

*!*	SELECT qtqk FROM hjxx WHERE dwbm=tcdwbm and grbm=tcgrbm and hjsj=tcjsnf+"."+tcjsyf and jllx=tcjslb into CURSOR curqtqk

IF tcjslb='其它情况'
	SEEK tcdwbm+tcgrbm+tcjslb+tcjsnf+tcjsyf order tag bmlb in qtqk
	IF FOUND("qtqk")
	    RETURN qtqk.content
	ELSE
	    RETURN ""
	ENDIF
ELSE
    oldalias=ALIAS()
    IF tcjslb='降资处分'
        tcjslb='降职处分'
    ENDIF
*!*		SEEK tcdwbm+tcgrbm+tcjslb+"  "+tcjsnf+"."+tcjsyf order tag bmlb in hjxx
	SELECT * FROM hjxx WHERE dwbm=tcdwbm AND grbm=tcgrbm AND hjsj=tcjsnf+"."+tcjsyf AND AT("处分",jllx)>0 INTO CURSOR qtqk
	IF !EMPTY(oldalias)
	    SELECT (oldalias)
	ENDIF
	RETURN qtqk.qtqk
	IF FOUND("hjxx")
	    RETURN hjxx.qtqk
	ELSE
	    RETURN ""
	ENDIF
ENDIF

*!*	IF _tally>0
*!*	    RETURN curqtqk.qtqk
*!*	ELSE
*!*	    RETURN ''
*!*	ENDIF
*!*	IF !EMPTY(talias)
*!*	    SELECT (talias)
*!*	ENDIF
