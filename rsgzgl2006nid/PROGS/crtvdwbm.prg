PROCEDURE crtvdwbm

PARAMETERS ttUpdates,tcConn

IF USED("dwbm")
    USE IN dwbm
ENDIF

*!*	IF m.czy='管理员'
*!*	    SQLEXEC(tcConn,"select * from dwbm order by dwbm","dwbm")
*!*	ELSE
*!*	    SQLEXEC(tcConn,"select * from dwbm where LEFT(dwbm,"+STR(LEN(ALLTRIM(m.czy)))+")='"+ALLTRIM(m.czy)+"' order by dwbm","dwbm")
*!*	ENDIF

IF INLIST(LEFT(m.czy,1),'0','1','2','3','4','5','6','7','8','9')&&单位管理员
    SQLEXEC(tcConn,"select * from dwbm where LEFT(dwbm,"+STR(LEN(ALLTRIM(m.czy)))+")='"+ALLTRIM(m.czy)+"' order by dwbm","dwbm")
ELSE
    aa=SQLEXEC(tcConn,"select * from dwbm order by dwbm","dwbm")
ENDIF
   
IF ttUpdates
	CURSORSETPROP("tables","dwbm","dwbm")
	CURSORSETPROP("keyfieldlist","id","dwbm")

	fn=""
	FOR i=1 TO FCOUNT("dwbm")
	    IF FIELD(i,"dwbm")<>"ID"
            fn=fn+","+FIELD(i,"dwbm")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"dwbm")

	fn=""
	FOR i=1 TO FCOUNT("dwbm")
        fn=fn+","+FIELD(i,"dwbm")+" dwbm."+FIELD(i,"dwbm")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"dwbm")

	CURSORSETPROP("sendupdates",.t.,"dwbm")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT dwbm
INDEX ON dwbm TAG dwbm ADDITIVE