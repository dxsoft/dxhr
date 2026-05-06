FUNCTION tg_jy_jb

PARAMETERS ttgnx,trznx,tzwbm,tjb,tdc,tdwbm,tgrbm,tjsny,tzjbm,tsfjzgb

LOCAL xjb,xdc,tgzw


&&职务高套的，级别未达最低，进最低，已达最低进一级，其他不变

tgzwbm=tg_jy_zw(ttgnx,trznx,tzwbm,tdwbm,tgrbm,tjsny,tzjbm,tsfjzgb)

m.xjb=LEFT(jbscope(tgzwbm),2)

IF tzjbm>tzwbm AND tsfjzgb="是" AND !EMPTY(tsfjzgb)
    xjb=tjb
    xdc=tdc
ELSE      
*!*		DO case
*!*		CASE tzjbm="01FF"
*!*		    xjb=""
*!*		    xdc=""
*!*		    
*!*		CASE tzjbm="01C0" AND tgzwbm="0206"
*!*		    IF tjb>m.xjb
*!*		        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
*!*		    ELSE
*!*		        xjb=STR(VAL(tjb)-1,2)    
*!*		        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
*!*		    ENDIF
*!*		    
*!*		CASE tzjbm="01B0" AND tgzwbm="0205"
*!*		    IF tjb>m.xjb
*!*		        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
*!*		    ELSE
*!*		        xjb=STR(VAL(tjb)-1,2)
*!*		        xdc=jbjs06(tjb,tdc,xjb,"200607")
*!*		    ENDIF

*!*		CASE LEFT(tzjbm,3)="01A" AND tgzwbm="0203"
*!*		    IF tjb>m.xjb
*!*		        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
*!*		    ELSE
*!*		        xjb=STR(VAL(tjb)-1,2)
*!*		        xdc=jbjs06(tjb,tdc,xjb,"200607")
*!*		    ENDIF

*!*		CASE LEFT(tzjbm,3)="01A" AND tgzwbm="0204"
*!*		    IF tjb>m.xjb
*!*		        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
*!*		    ELSE
*!*		        xjb=tjb
*!*		        xdc=tdc
*!*		    ENDIF

*!*		OTHERWISE
*!*		    xjb=tjb
*!*		    xdc=tdc
*!*		ENDCASE

	DO case
	CASE tzwbm="01FF"
	    xjb=""
	    xdc=""
	    
	CASE tzwbm="01C0" AND tgzwbm="0206"
	    IF tjb>m.xjb&&未达最低进最低
	        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
	    ELSE&&已达最低进一级
	        xjb=STR(VAL(tjb)-1,2)    
	        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
	    ENDIF
	    
	CASE tzwbm="01B0" AND tgzwbm="0205"
	    IF tjb>m.xjb
	        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
	    ELSE
	        xjb=STR(VAL(tjb)-1,2)
	        xdc=jbjs06(tjb,tdc,xjb,"200607")
	    ENDIF

	CASE LEFT(tzwbm,3)="01A" AND tgzwbm="0203"
	    IF tjb>m.xjb
	        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
	    ELSE
	        xjb=STR(VAL(tjb)-1,2)
	        xdc=jbjs06(tjb,tdc,xjb,"200607")
	    ENDIF

	CASE LEFT(tzwbm,3)="01A" AND tgzwbm="0204"
	    IF tjb>m.xjb
	        xdc=jbjs06(tjb,tdc,m.xjb,"200607")
	    ELSE
	        xjb=tjb
	        xdc=tdc
	    ENDIF

	OTHERWISE
	    xjb=tjb
	    xdc=tdc
	ENDCASE
ENDIF

RETURN xjb+xdc