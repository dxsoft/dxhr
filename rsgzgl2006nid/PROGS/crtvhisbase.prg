FUNCTION crtvhisbase

PARAMETERS ttUpdates,tcConn

IF USED("hisbase")
    USE IN hisbase
ENDIF

IF EMPTY(m.pdwbm)
	nn=SQLEXEC(tcConn,"select *,000000 as zze,999999999 as xh from hisbase","hisbase")
ELSE
*!*	   	nn=SQLEXEC(tcConn,"select t.*,t.hj2-b.hj2 as zze,999999999 as xh  from (select * from hisbase where dwbm='"+m.pdwbm+"') t left join hisbase b on t.id=b.sid","hisbase")

   	nn=SQLEXEC(tcConn,"select *,000000 as zze,999999999 as xh from hisbase where dwbm='"+ALLTRIM(m.pdwbm)+"'","hisbase")

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

INDEX on dwbm+grbm+jsnf+jsyf+STR(hj2)+bbz TAG bmlbhj ADDITIVE
INDEX ON dwbm+grbm+sid TAG xbm ADDITIVE
INDEX ON dwbm+grbm+id TAG bm ADDITIVE
INDEX ON id TAG id ADDITIVE

CURSORSETPROP("Buffering",5,"hisbase")


