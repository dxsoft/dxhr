FUNCTION crtvndkhbb

PARAMETERS ttUpdate,tcConn

IF USED("ndkhbb")
    USE IN ndkhbb
ENDIF

SQLEXEC(tcConn,"select * from dndkh where dwbm='"+m.pdwbm+"'","ndkhbb")

IF ttupdate
	CURSORSETPROP("tables","dndkh","ndkhbb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,khnd","ndkhbb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhbb")
        fn=fn+","+FIELD(i,"ndkhbb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ndkhbb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhbb")
        fn=fn+","+FIELD(i,"ndkhbb")+" dndkh."+FIELD(i,"ndkhbb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ndkhbb")

	CURSORSETPROP("sendupdates",.t.,"ndkhbb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ndkhbb
INDEX ON khnd+dwbm+grbm TAG ndbm

