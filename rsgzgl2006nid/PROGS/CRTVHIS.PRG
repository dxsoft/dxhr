FUNCTION crtvhis

PARAMETERS ttUpdates

************
CREATE SQL VIEW his AS SELECT Hisbase.*;
 FROM ;
     gzjsgl!hisbase;
 WHERE  Hisbase.dwbm = ( ?m.pdwbm );
 ORDER BY Hisbase.dwbm, Dryjbxx.grbm

DBSetProp("his","View","SendUpdates",ttUpdates)
DBSetProp("his","View","BatchUpdateCount",1)
DBSetProp("his","View","CompareMemo",.T.)
DBSetProp("his","View","FetchAsNeeded",.F.)
DBSetProp("his","View","FetchMemo",.T.)
DBSetProp("his","View","FetchSize",100)
DBSetProp("his","View","MaxRecords",-1)
DBSetProp("his","View","Prepared",.F.)
DBSetProp("his","View","UpdateType",1)
DBSetProp("his","View","UseMemoSize",255)
DBSetProp("his","View","Tables","gzjsgl!hisbase")
DBSetProp("his","View","WhereType",3)

IF !USED("hisbase")
    USE hisbase IN 0
ENDIF

FOR i=1 TO FCOUNT("hisbase")
    fn="HIS"+"."+FIELD(i,"hisbase")
    fn1="gzjsgl!hisbase."+FIELD(i,"hisbase")
    DT=DBGETPROP(fn,"FIELD","DataType")
    DBSetProp(fn,"Field","DataType",DT)
    DBSETPROP(fn,"field","updatename",fn1)
    DBSETPROP(fn,"field","updatable",.T.)
	IF INLIST(fn,"HIS.DWBM","HIS.GRBM")
		DBSetProp(fn,"Field","KeyField",.T.)
	ENDIF
ENDFOR
USE IN hisbase