FUNCTION crtvndkh

PARAMETERS ttUpdate,tcConn

IF USED("ndkh")
    USE IN ndkh
ENDIF

IF EMPTY(m.pdwbm)
    SQLEXEC(tcConn,"select * from dndkh","ndkh")
ELSE
    SQLEXEC(tcConn,"select * from dndkh where dwbm='"+ALLTRIM(m.pdwbm)+"'","ndkh")
ENDIF

IF ttupdate
	CURSORSETPROP("tables","dndkh","ndkh")

*!*		CURSORSETPROP("keyfieldlist","dwbm,grbm,khnd","ndkh")

	CURSORSETPROP("keyfieldlist","id","ndkh")

	fn=""
	FOR i=1 TO FCOUNT("ndkh")
	    IF FIELD(i,"ndkh")<>"ID"
            fn=fn+","+FIELD(i,"ndkh")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ndkh")

	fn=""
	FOR i=1 TO FCOUNT("ndkh")
        fn=fn+","+FIELD(i,"ndkh")+" dndkh."+FIELD(i,"ndkh")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ndkh")

	CURSORSETPROP("sendupdates",.t.,"ndkh")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ndkh
INDEX ON khnd+dwbm+grbm TAG ndbm

