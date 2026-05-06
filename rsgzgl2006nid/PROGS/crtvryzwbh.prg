FUNCTION crtvryzwbh

PARAMETERS ttUpdate,tcConn

IF USED("ryzwbh")
    USE IN ryzwbh
ENDIF

m=SQLEXEC(tcConn,"select * from dryzwbh where dwbm='"+ALLTRIM(m.pdwbm)+"'","ryzwbh")

IF ttUpdate
	CURSORSETPROP("tables","dryzwbh","ryzwbh")

*!*		CURSORSETPROP("keyfieldlist","dwbm,grbm,zwbm,srny","ryzwbh")

	CURSORSETPROP("keyfieldlist","id","ryzwbh")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbh")
	    IF FIELD(i,"ryzwbh")<>"ID"
	        fn=fn+","+FIELD(i,"ryzwbh")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryzwbh")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbh")
	    fn=fn+","+FIELD(i,"ryzwbh")+" dryzwbh."+FIELD(i,"ryzwbh")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryzwbh")

	CURSORSETPROP("sendupdates",.t.,"ryzwbh")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryzwbh
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+zwbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
INDEX ON dwbm+grbm+LEFT(zwbm,2)+srny TAG bmlbny ADDITIVE

SET ORDER TO srny IN ryzwbh