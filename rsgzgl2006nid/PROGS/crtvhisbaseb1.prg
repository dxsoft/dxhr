FUNCTION crtvhisbaseb1

PARAMETERS ttUpdates,tcConn

IF USED("xgz")
    USE IN xgz
ENDIF

IF EMPTY(m.pdwbm)
	nn=SQLEXEC(tcConn,"","xgz")
ELSE
   	nn=SQLEXEC(tcConn,"select * from hisbaseb where dwbm='"+ALLTRIM(m.pdwbm)+"' and sid=''","xgz")
ENDIF

*!*	AERROR(aa)

IF ttUpdates
	CURSORSETPROP("tables","hisbaseb","xgz")

	CURSORSETPROP("keyfieldlist","id","xgz")

	fn=""
	FOR i=1 TO FCOUNT("xgz")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
	    	fn=fn+","+FIELD(i,"xgz")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"xgz")

	fn=""
	FOR i=1 TO FCOUNT("xgz")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
    	    fn=fn+","+FIELD(i,"xgz")+" xgz."+FIELD(i,"xgz")
    	ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"xgz")

	CURSORSETPROP("sendupdates",.t.,"xgz")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT xgz

*!*	INDEX ON dwbm+grbm+jsnf+jsyf+jslb+zwbm1+jbgzjb1+zwgzdc1+tbnd1 TAG bmlb ADDITIVE


*!*	INDEX ON dwbm+grbm+zwbm1+jbgzjb1+zwgzdc1+tbnd1+jbtbz1+jx1+jxjtbz1 TAG bmlbhj1 ADDITIVE


*!*	INDEX ON dwbm+grbm+jsnf+jsyf+jslb+zwbm2+jbgzjb2+zwgzdc2+tbnd TAG xbmlb ADDITIVE&&查是否存在当前记录
*!*	INDEX ON dwbm+grbm+yznf+yzyf+yzjslb+zwbm2+jbgzjb2+zwgzdc2+tbnd TAG yzbm ADDITIVE



INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2,5)+bbz TAG bmlbhj ADDITIVE

*!*	INDEX ON xh TAG xh ADDITIVE

*!*	INDEX ON dwbm+grbm+jslb+jsnf+jsyf TAG next ADDITIVE
*!*	INDEX ON dwbm+grbm+yzjslb+yznf+yzyf TAG pre ADDITIVE

INDEX ON dwbm+grbm+sid TAG xbm ADDITIVE