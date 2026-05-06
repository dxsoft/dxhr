FUNCTION crtvtgxx

PARAMETERS ttUpdate,tcConn

IF USED("tgxx")
    USE IN tgxx
ENDIF

SQLEXEC(tcConn,"select * from dtgxx where dwbm='"+ALLTRIM(m.pdwbm)+"'","tgxx")

IF ttUpdate
	CURSORSETPROP("tables","dtgxx","tgxx")

*!*		CURSORSETPROP("keyfieldlist","dwbm,grbm","tgxx")

	CURSORSETPROP("keyfieldlist","id","tgxx")

	fn=""
	FOR i=1 TO FCOUNT("tgxx")
	    IF FIELD(i,"tgxx")<>"ID"
            fn=fn+","+FIELD(i,"tgxx")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgxx")

	fn=""
	FOR i=1 TO FCOUNT("tgxx")
        fn=fn+","+FIELD(i,"tgxx")+" dtgxx."+FIELD(i,"tgxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgxx")

	CURSORSETPROP("sendupdates",.t.,"tgxx")
	CURSORSETPROP("wheretype",2)
ENDIF
