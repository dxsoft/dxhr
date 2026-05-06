PARAMETERS tczwbm,tcxl,tcTbnd,tnXlgzse,tcXlzwdc

tnXlgzse=0
tcXlzwdc=""

IF !EMPTY(tcXl)
	SELECT xldzbz&&学历套
    LOCATE FOR xl=ALLTRIM(tcXl) AND LEFT(zwbm,2)=LEFT(tcZwbm,2)
    tcXlzwmc=xldzbz.zwmc
    tcXlzwbm=xldzbz.zwbm
    tcXlzwdc=xldzbz.gzdc
    tnXlgzse=zwgzse(xldzbz.Zwbm,xldzbz.gzdc,tcTbnd)

	&&就近就高套入高n档
    SEEK tcTbnd+tczwbm ORDER ndbm IN zwgzbz

    IF FOUND('zwgzbz')
	    FOR j=1 to 16
	        v_j=allt(str(j))
	        IF tnXlgzse<=zwgzbz.a&v_j
	            tcXlzwdc=v_j
	            tnXlgzse=zwgzbz.a&v_j 
	            EXIT
	        ENDIF
	    ENDFOR
	ENDIF
ENDIF
