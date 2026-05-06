FUNCTION crtvhjxxb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from hjxxb where dwbm='"+ALLTRIM(m.pdwbm)+"'","hjxxb")

IF ttUpdates
	CURSORSETPROP("tables","hjxxb","hjxxb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,hjmc,hjsj","hjxxb")

	fn=""
	FOR i=1 TO FCOUNT("hjxxb")
    	IF FIELD(i,"hjxxb")<>"ID"
            fn=fn+","+FIELD(i,"hjxxb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hjxxb")

	fn=""
	FOR i=1 TO FCOUNT("hjxxb")
        fn=fn+","+FIELD(i,"hjxxb")+" hjxxb."+FIELD(i,"hjxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hjxxb")

	CURSORSETPROP("sendupdates",.t.,"hjxxb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hjxxb
INDEX ON dwbm+grbm+tqyjjssj TAG bm addi
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE