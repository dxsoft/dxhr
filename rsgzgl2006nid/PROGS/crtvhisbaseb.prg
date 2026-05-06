FUNCTION crtvhisbaseb

PARAMETERS ttUpdates,tcConn

IF USED("hisbaseb")
    USE IN hisbaseb
ENDIF

*!*	SQLEXEC(tcConn,"select *,hj2-hj1 as zze,99 as xh from hisbaseb where dwbm='"+m.pdwbm+"'","hisbaseb")


IF EMPTY(m.pdwbm)
	nn=SQLEXEC(tcConn,"select *,000000 as zze,999999999 as xh from hisbaseb","hisbaseb")
ELSE
   	nn=SQLEXEC(tcConn,"select *,000000 as zze,999999999 as xh from hisbaseb where dwbm='"+ALLTRIM(m.pdwbm)+"'","hisbaseb")
ENDIF





IF ttUpdates
	CURSORSETPROP("tables","hisbaseb","hisbaseb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,jslb,jsnf,jsyf","hisbaseb")

	fn=""
	FOR i=1 TO FCOUNT("hisbaseb")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
	    	fn=fn+","+FIELD(i,"hisbaseb")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hisbaseb")

	fn=""
	FOR i=1 TO FCOUNT("hisbaseb")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
    	    fn=fn+","+FIELD(i,"hisbaseb")+" hisbaseb."+FIELD(i,"hisbaseb")
    	ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hisbaseb")

	CURSORSETPROP("sendupdates",.t.,"hisbaseb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hisbaseb
*!*	UPDATE hisbaseb SET zze=hj2 WHERE ISNULL(zze)

INDEX ON dwbm+grbm+jsnf+jsyf+jslb+zwbm2+jbgzjb2+zwgzdc2+tbnd TAG xbmlb ADDITIVE&&查是否存在当前记录
INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2,5)+bbz TAG bmlbhj ADDITIVE
INDEX ON xh TAG xh ADDITIVE
