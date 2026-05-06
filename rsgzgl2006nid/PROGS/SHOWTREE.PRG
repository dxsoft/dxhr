Function showtree

parameter tcFldname,tcLeft,tcTop,tcFilter,retcodelen

sele fldprop
loca for upper(field_name)=upper(tcFldname)

cDwbm=""
dime aSeleitem[1,2]

if !empty(fldprop.dmlb)

	cFilter="bm=fldprop.dmlb and len(alltrim(bm))>=5 and sfsy and "+iif(empty(tcfilter),".t.",tcFilter)

	select bm,mc from dmb dist order by bm where &cFilter into array aSeleItem

	if _tally<>0
	    DO form seleTree with aSeleItem,tcLeft,tcTop,retcodelen to cDwbm name aa
	endif

	IF !EMPTY(cDwbm)
		FOR i=1 TO alen(aSeleItem,1)
		    IF aSeleItem[i,1]=m.cDwbm
		        RETURN aSeleItem[i,2]
		        EXIT
		    ENDIF
		ENDFOR
	ELSE
        RETURN ""
    ENDIF
ELSE
    RETURN ""
ENDIF
