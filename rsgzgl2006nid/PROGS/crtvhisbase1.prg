FUNCTION crtvhisbase1

PARAMETERS ttUpdates,tcConn

IF USED("hisbase")
    TABLEREVERT(.t.,"hisbase")
    USE IN hisbase
ENDIF

IF EMPTY(m.pdwbm)
	nn=SQLEXEC(tcConn,"select * from hisbase where sid=''","hisbase")
ELSE
   	nn=SQLEXEC(tcConn,"select * from hisbase where dwbm='"+ALLTRIM(m.pdwbm)+"' and sid=''","hisbase")
ENDIF

*!*	AERROR(aa)

IF ttUpdates
	CURSORSETPROP("tables","hisbase","hisbase")

	CURSORSETPROP("keyfieldlist","id","hisbase")

	fn=""
	FOR i=1 TO FCOUNT("hisbase")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
	    	fn=fn+","+FIELD(i,"hisbase")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hisbase")

	fn=""
	FOR i=1 TO FCOUNT("hisbase")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
    	    fn=fn+","+FIELD(i,"hisbase")+" hisbase."+FIELD(i,"hisbase")
    	ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hisbase")

	CURSORSETPROP("sendupdates",.t.,"hisbase")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hisbase

*!*	INDEX ON dwbm+grbm+jsnf+jsyf+jslb+zwbm1+jbgzjb1+zwgzdc1+tbnd1 TAG bmlb ADDITIVE


*!*	INDEX ON dwbm+grbm+zwbm1+jbgzjb1+zwgzdc1+tbnd1+jbtbz1+jx1+jxjtbz1 TAG bmlbhj1 ADDITIVE


*!*	INDEX ON dwbm+grbm+jsnf+jsyf+jslb+zwbm2+jbgzjb2+zwgzdc2+tbnd TAG xbmlb ADDITIVE&&查是否存在当前记录
*!*	INDEX ON dwbm+grbm+yznf+yzyf+yzjslb+zwbm2+jbgzjb2+zwgzdc2+tbnd TAG yzbm ADDITIVE



INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2,5) TAG bmlbhj ADDITIVE

*!*	INDEX ON xh TAG xh ADDITIVE

*!*	INDEX ON dwbm+grbm+jslb+jsnf+jsyf TAG next ADDITIVE
*!*	INDEX ON dwbm+grbm+yzjslb+yznf+yzyf TAG pre ADDITIVE

INDEX ON dwbm+grbm+sid TAG xbm ADDITIVE