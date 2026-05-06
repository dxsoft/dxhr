FUNCTION crtvryzwbhb

PARAMETERS ttUpdate,tcConn

IF USED("ryzwbhb")
    USE IN ryzwbhb
ENDIF

m=SQLEXEC(tcConn,"select * from dryzwbhb where dwbm='"+ALLTRIM(m.pdwbm)+"'","ryzwbhb")

IF ttUpdate
	CURSORSETPROP("tables","dryzwbhb","ryzwbhb")
	CURSORSETPROP("keyfieldlist","id","ryzwbhb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhb")
	    IF FIELD(i,"ryzwbhb")<>"ID"	
	        fn=fn+","+FIELD(i,"ryzwbhb")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryzwbhb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhb")
	    fn=fn+","+FIELD(i,"ryzwbhb")+" dryzwbhb."+FIELD(i,"ryzwbhb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryzwbhb")

	CURSORSETPROP("sendupdates",.t.,"ryzwbhb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryzwbhb
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
INDEX ON dwbm+grbm+LEFT(zwbm,2)+srny TAG bmlbny ADDITIVE
SET ORDER TO srny IN ryzwbhb