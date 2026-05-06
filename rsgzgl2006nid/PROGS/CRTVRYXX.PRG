FUNCTION crtvryxx

PARAMETERS ttUpdates

************
CREATE SQL VIEW ryxx AS SELECT Dryjbxx.*;
 FROM ;
     gzjsgl!dryjbxx;
 WHERE  Dryjbxx.dwbm = ( ?m.pdwbm );
 ORDER BY Dryjbxx.dwbm, Dryjbxx.grbm

DBSetProp("ryjbxx","View","SendUpdates",ttUpdates)
DBSetProp("ryjbxx","View","BatchUpdateCount",1)
DBSetProp("ryjbxx","View","CompareMemo",.T.)
DBSetProp("ryjbxx","View","FetchAsNeeded",.F.)
DBSetProp("ryjbxx","View","FetchMemo",.T.)
DBSetProp("ryjbxx","View","FetchSize",100)
DBSetProp("ryjbxx","View","MaxRecords",-1)
DBSetProp("ryjbxx","View","Prepared",.F.)
DBSetProp("ryjbxx","View","UpdateType",1)
DBSetProp("ryjbxx","View","UseMemoSize",255)
DBSetProp("ryjbxx","View","Tables","gzjsgl!dryjbxx")
DBSetProp("ryjbxx","View","WhereType",3)

IF !USED("dryjbxx")
    USE dryjbxx IN 0
ENDIF

FOR i=1 TO FCOUNT("dryjbxx")
    fn="RYJBXX"+"."+FIELD(i,"dryjbxx")
    fn1="gzjsgl!dryjbxx."+FIELD(i,"dryjbxx")
    DT=DBGETPROP(fn,"FIELD","DataType")
    DBSetProp(fn,"Field","DataType",DT)
    DBSETPROP(fn,"field","updatename",fn1)
    DBSETPROP(fn,"field","updatable",.T.)
	IF INLIST(fn,"RYJBXX.DWBM","RYJBXX.GRBM")
		DBSetProp(fn,"Field","KeyField",.T.)
	ENDIF
ENDFOR
USE IN dryjbxx