FUNCTION crtvjx

PARAMETERS ttUpdates,tcConn

IF USED("jx")
    USE IN jx
ENDIF

SQLEXEC(tcConn,'update jx set xrjxbz=0')

IF UPPER(dbtype)='MYSQL'
    SQLEXEC(tcConn, 'UPDATE jx JOIN ( SELECT dwbm, grbm, MAX(sysj) AS max_sysj FROM jx GROUP BY dwbm, grbm) AS temp_jx ON jx.dwbm = temp_jx.dwbm AND jx.grbm = temp_jx.grbm AND jx.sysj = temp_jx.max_sysj SET jx.xrjxbz = 1')
ELSE
	SQLEXEC(tcConn, 'update jx set xrjxbz=1 where EXISTS (SELECT dwbm,grbm,max(sysj) FROM jx group by dwbm,grbm)')
ENDIF

SQLEXEC(tcConn,"select * from jx where dwbm='"+ALLTRIM(m.pdwbm)+"'","jx")

IF ttUpdates
	CURSORSETPROP("tables","jx","jx")
	CURSORSETPROP("keyfieldlist","id","jx")

	fn=""
	FOR i=1 TO FCOUNT("jx")
	    IF FIELD(i,"jx")<>"ID"
            fn=fn+","+FIELD(i,"jx")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jx")

	fn=""
	FOR i=1 TO FCOUNT("jx")
        fn=fn+","+FIELD(i,"jx")+" jx."+FIELD(i,"jx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jx")

	CURSORSETPROP("sendupdates",.t.,"jx")
	CURSORSETPROP("wheretype",2,"jx")
ENDIF

SELECT jx
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON sysj TAG sysj ADDITIVE
INDEX ON dwbm+grbm+jx TAG bmjx ADDITIVE
