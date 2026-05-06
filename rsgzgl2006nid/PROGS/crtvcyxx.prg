FUNCTION crtvcyxx

PARAMETERS ttUpdates,tcConn

IF USED("cyxx")
    USE IN cyxx
ENDIF

*!*	SQLEXEC(tcConn,"select ID,dwbm,dwmc,dwjc,zgry,szds,zzrs,skbz,shrq,yxhjsz,bz,zwbh10,zzdz10,qytrlxjs,qytrtg93,trdz10,zyjrlxjs,zydnjd,tgdcgljs,"+"tf,xsdwywqx,blxs,swyz,xsdwxg,jzmcbtmc,sdbtmc,spfs,dwsplb,jqdm,ltjddc,jxgz,jjjy,fdgz,pgbc,softsn,sp,pict,path_bak,zwbhhjsdj,cdchjsdj,autobak,ask,chkupdate from cyxx","cyxx")

SQLEXEC(tcConn,"select * from cyxx","cyxx")

IF ttUpdates
	CURSORSETPROP("tables","cyxx","cyxx")
	CURSORSETPROP("keyfieldlist","id","cyxx")

	fn=""
	FOR i=1 TO FCOUNT("cyxx")
        fn=fn+","+FIELD(i,"cyxx")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"cyxx")

	fn=""
	FOR i=1 TO FCOUNT("cyxx")
        fn=fn+","+FIELD(i,"cyxx")+" cyxx."+FIELD(i,"cyxx")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"cyxx")

	CURSORSETPROP("sendupdates",.t.,"cyxx")
	CURSORSETPROP("wheretype",2)
ENDIF