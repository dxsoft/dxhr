FUNCTION crtvfldjbxx

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select sequence,field_cate,must,category,sfsy,xzry,isgz,property,readonly,tblname,field_name,field_type,field_len,field_dec,field_cap,field_capj,dmlb,field_null,canbyhand,isgroup,iscount from fldjbxx","fldjbxx")

IF ttUpdates
	CURSORSETPROP("tables","fldjbxx","fldjbxx")
	CURSORSETPROP("keyfieldlist","tblname,field_name","fldjbxx")

	fn=""
	FOR i=1 TO FCOUNT("fldjbxx")
        fn=fn+","+FIELD(i,"fldjbxx")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"fldjbxx")

	fn=""
	FOR i=1 TO FCOUNT("fldjbxx")
        fn=fn+","+FIELD(i,"fldjbxx")+" fldjbxx."+FIELD(i,"fldjbxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"fldjbxx")

	CURSORSETPROP("sendupdates",.t.,"fldjbxx")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT fldjbxx
INDEX ON UPPER(field_name) TAG fldname ADDITIVE