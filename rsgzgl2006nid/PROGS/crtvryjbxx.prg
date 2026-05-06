FUNCTION crtvryjbxx

PARAMETERS ttUpdates,tcConn

IF USED("ryjbxx")
    USE IN ryjbxx
ENDIF

IF EMPTY(m.pdwbm)
    SQLEXEC(tcConn,"select * from dryjbxx where grbm<>'' order by grbm","ryjbxx")
ELSE
    SQLEXEC(tcConn,"select * from dryjbxx where dwbm='"+ALLTRIM(m.pdwbm)+"' and grbm<>'' order by grbm","ryjbxx")
ENDIF

IF ttUpdates
	CURSORSETPROP("tables","dryjbxx","ryjbxx")

*!*		CURSORSETPROP("keyfieldlist","dwbm,grbm","ryjbxx")

	CURSORSETPROP("keyfieldlist","uid","ryjbxx")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxx")
	    IF FIELD(i,"ryjbxx")<>"UID"
            fn=fn+","+FIELD(i,"ryjbxx")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryjbxx")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxx")
        fn=fn+","+FIELD(i,"ryjbxx")+" dryjbxx."+FIELD(i,"ryjbxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryjbxx")

	CURSORSETPROP("sendupdates",.t.,"ryjbxx")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryjbxx
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm TAG dwbm ADDITIVE

