FUNCTION crtvryzwbhbb

PARAMETERS ttUpdate,tcConn

IF USED("ryzwbhbb")
    USE IN ryzwbhbb
ENDIF

m=SQLEXEC(tcConn,"select * from dryzwbh where dwbm='"+ALLTRIM(m.pdwbm)+"'","ryzwbhbb")

IF ttUpdate
	CURSORSETPROP("tables","dryzwbh","ryzwbhbb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zwbm,srny","ryzwbhbb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhbb")
	    fn=fn+","+FIELD(i,"ryzwbhbb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryzwbhbb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhbb")
	    fn=fn+","+FIELD(i,"ryzwbhbb")+" dryzwbh."+FIELD(i,"ryzwbhbb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryzwbhbb")

	CURSORSETPROP("sendupdates",.t.,"ryzwbhbb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryzwbhbb
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+zwbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
SET ORDER TO srny IN ryzwbh