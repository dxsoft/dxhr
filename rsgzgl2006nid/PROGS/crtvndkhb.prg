FUNCTION crtvndkhb

PARAMETERS ttUpdate,tcConn

IF USED("ndkhb")
    USE IN ndkhb
ENDIF

SQLEXEC(tcConn,"select * from dndkhb where dwbm='"+ALLTRIM(m.pdwbm)+"'","ndkhb")

IF ttupdate
	CURSORSETPROP("tables","dndkhb","ndkhb")
	CURSORSETPROP("keyfieldlist","id","ndkhb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhb")
	    IF FIELD(i,"ndkhb")<>"ID"
            fn=fn+","+FIELD(i,"ndkhb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ndkhb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhb")
        fn=fn+","+FIELD(i,"ndkhb")+" dndkhb."+FIELD(i,"ndkhb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ndkhb")

	CURSORSETPROP("sendupdates",.t.,"ndkhb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ndkhb
INDEX ON khnd+dwbm+grbm TAG ndbm

