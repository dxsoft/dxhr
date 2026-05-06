FUNCTION crtvtgxxb

PARAMETERS ttUpdate,tcConn

IF USED("tgxxb")
    USE IN tgxxb
ENDIF


SQLEXEC(tcConn,"select * from dtgxxb where dwbm='"+ALLTRIM(m.pdwbm)+"'","tgxxb")

IF ttUpdate
	CURSORSETPROP("tables","dtgxxb","tgxxb")
	CURSORSETPROP("keyfieldlist","id","tgxxb")

	fn=""
	FOR i=1 TO FCOUNT("tgxxb")
	    IF FIELD(i,"tgxxb")<>"ID"
            fn=fn+","+FIELD(i,"tgxxb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgxxb")

	fn=""
	FOR i=1 TO FCOUNT("tgxxb")
        fn=fn+","+FIELD(i,"tgxxb")+" dtgxxb."+FIELD(i,"tgxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgxxb")

	CURSORSETPROP("sendupdates",.t.,"tgxxb")
	CURSORSETPROP("wheretype",2)
ENDIF

