FUNCTION crtview

PARAMETERS ttUpdates,tcConn

IF USED("hisbase")
    USE IN hisbase
ENDIF

SQLEXEC(tcConn,"select * from hisbase where dwbm='"+m.pdwbm+"'","hisbase")

IF ttUpdates
	CURSORSETPROP("tables","hisbase","hisbase")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,jslb,jsnf,jsyf","hisbase")

	fn=""
	FOR i=1 TO FCOUNT("hisbase")
	    fn=fn+","+FIELD(i,"hisbase")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hisbase")

	fn=""
	FOR i=1 TO FCOUNT("hisbase")
	    fn=fn+","+FIELD(i,"hisbase")+" hisbase."+FIELD(i,"hisbase")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hisbase")

	CURSORSETPROP("sendupdates",.t.,"hisbase")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hisbase
INDEX ON dwbm+grbm+jsnf+jsyf+jslb TAG bmlb
INDEX ON dwbm+grbm+yznf+yzyf+yzjslb TAG yzbmlb