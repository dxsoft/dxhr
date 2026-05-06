FUNCTION crtvjxgzb

PARAMETERS ttUpdates,tcConn

IF EMPTY(m.ny)
    SQLEXEC(tcConn,"select a.*,b.cjgzny,b.zzny,b.xb,0000 as hj from djxgzb a,dryjbxx b where a.dwbm='"+m.pdwbm+"' and a.dwbm=b.dwbm and a.grbm=b.grbm","jxgzb")
ELSE
    SQLEXEC(tcConn,"select a.*,b.cjgzny,b.zzny,b.xb,0000 as hj from djxgzb a,dryjbxx b where a.dwbm='"+m.pdwbm+"' and a.ny='"+m.ny+"' and a.dwbm=b.dwbm and a.grbm=b.grbm","jxgzb")
ENDIF

IF ttUpdates
	CURSORSETPROP("tables","djxgzb","jxgzb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,ny","jxgzb")

	fn=""
	FOR i=1 TO FCOUNT("jxgzb")
	    IF !INLIST(FIELD(i,"jxgzb"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgzb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxgzb")

	fn=""
	FOR i=1 TO FCOUNT("jxgzb")
	    IF !INLIST(FIELD(i,"jxgzb"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgzb")+" djxgzb."+FIELD(i,"jxgzb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxgzb")

	CURSORSETPROP("sendupdates",.t.,"jxgzb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxgzb
INDEX ON dwbm+grbm+ny TAG bm ADDITIVE

