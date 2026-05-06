FUNCTION crtvrptinfo

PARAMETERS ttUpdates,tcConn

IF USED("rptinfo")
    USE IN rptinfo
ENDIF

SQLEXEC(tcConn,"select * from rptinfo where bblb='' order by lbbm","rptinfo")

IF ttUpdates
	CURSORSETPROP("tables","rptinfo","rptinfo")
	CURSORSETPROP("keyfieldlist","lbbm","rptinfo")

	fn=""
	FOR i=1 TO FCOUNT("rptinfo")
        fn=fn+","+FIELD(i,"rptinfo")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"rptinfo")

	fn=""
	FOR i=1 TO FCOUNT("rptinfo")
        fn=fn+","+FIELD(i,"rptinfo")+" rptinfo."+FIELD(i,"rptinfo")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"rptinfo")

	CURSORSETPROP("sendupdates",.t.,"rptinfo")
	CURSORSETPROP("wheretype",2)
ENDIF
