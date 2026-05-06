FUNCTION Xlbydwgrbm

&&只为计算补发绩效工资用
PARAMETERS tcRybm

LOCAL tcdate
SEEK tcRybm ORDER dwgrbm IN dryjbxx
tcdate=dryjbxx.cjgzny

SELECT xlbm,xl,STRTRAN(bysj,".") FROM xl WHERE dwbm+grbm=tcrybm AND xllb<>"其它" AND STRTRAN(bysj,".")<STRTRAN(tcdate,".") ORDER BY xlbm,bysj DESC INTO ARRAY lsarray 
IF _tally>0
    RETURN lsarray[1,1]+RIGHT(SPACE(6)+ALLTRIM(lsarray[1,3]),6)+lsarray[1,2]
ENDIF

RETURN SPACE(36)
