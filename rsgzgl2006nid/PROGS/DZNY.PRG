FUNCTION dzny

PARAMETERS tcDrny,tcDrlx,tcZysj

IF tcDrlx='部队转业'
    RETURN tcZysj
ELSE
    v_jsnf=left(tcDrny,4)
    v_jsyf=allt(str(val(subs(tcDrny,6,2))+1))
	IF v_jsyf='13'
	    v_jsyf='01'
	    v_jsnf=allt(str(val(v_jsnf)+1))
	ENDIF
    RETURN v_jsnf+'.'+v_jsyf
ENDIF