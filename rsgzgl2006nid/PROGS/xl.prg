FUNCTION Xl

PARAMETERS tcRybm,tcDate

oldalias=alias()

SELECT xlbm,xl,STRTRAN(bysj,".") FROM xl WHERE dwbm+grbm=tcrybm AND xllb<>"ÆäËü" AND STRTRAN(bysj,".","")<=STRTRAN(tcdate,".","") AND !EMPTY(STRTRAN(bysj,".","")) ORDER BY xlbm,bysj DESC INTO ARRAY lsarray 
select (oldalias)
IF _tally>0
     RETURN lsarray[1,1]+RIGHT(SPACE(6)+ALLTRIM(lsarray[1,3]),6)+lsarray[1,2]
ENDIF

RETURN SPACE(36)
