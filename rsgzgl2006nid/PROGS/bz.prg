FUNCTION bz

PARAMETERS tdwbm,tgrbm,tjsnf,tjsyf,tjrny,tzzsj

LOCAL latzwbh,cexpr
DIMENSION latzwbh[1]

SELECT zwgw1,zwgw2,jsnf,jsyf FROM hisbase WHERE dwbm=tdwbm AND grbm=tgrbm AND jslb="职务变化" AND jsnf+jsyf>tjsnf+tjsyf INTO ARRAY latzwbh
IF _tally>0
    cexpr=latzwbh[1,3]+"."+latzwbh[1,4]+"由"+ALLTRIM(latzwbh[1,1])+"晋升"
ELSE
    cexpr= ""
ENDIF

IF TYPE("tjrny")="C"
	IF STRTRAN(tjrny,".","")>jsnf+jsyf
	    cexpr=cexpr+tjrny+"进入本单位"
	ENDIF
ENDIF

IF TYPE("tzzsj")="C"
	IF STRTRAN(tzzsj,".","")>jsnf+jsyf
	    cexpr=cexpr+tzzsj+"转正定级"
	ENDIF
ENDIF

RETURN LEFT(ALLTRIM(cexpr)+SPACE(50),50)
