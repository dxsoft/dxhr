FUNCTION initdataen

PARAMETERS tblist

conn=sqlstringconnect("DRIVER=SQL Server Native Client 10.0;SERVER=SERVER\SQLEXPRESS;UID=Administrator;Trusted_Connection=Yes;Database = gzjsgl",.t.)

IF EMPTY(tblist)
    RETURN
ENDIF

i=1
nn=AT(",",tblist,i)
IF nn=0
    IF USED(tblist)
        USE IN &tblist
    ENDIF
    crtv&tblist(.t.,conn)
ENDIF

DO WHILE nn>0
    i=i+1
    mm=AT(",",tblist,i)
    IF mm>0
ENDIF
endd
    
IF cate
	crtvbz(conn)
	crtvdwbm(.f.,conn)
ENDIF

IF USED("hisbase")
    USE IN hisbase
ENDIF
crtvgztz(.T.,conn)

IF USED("ryzwbh")
    USE IN ryzwbh
ENDIF
crtvryzwbh(conn)

IF USED("ndkh")
    USE IN ndkh
ENDIF
crtvndkh(conn)

IF USED("jxgz")
    USE IN jxgz
ENDIF
crtvjxgz(conn)

IF USED("xl")
    USE IN xl
ENDIF
crtvxl(conn)

IF USED("tgxx")
    USE IN tgxx
ENDIF
crtvtgxx(.T.,conn)

IF USED("tgqgz2006")
    USE IN tgqgz2006
ENDIF
crtvtgqgz(conn)
 
IF USED("ryjbxx")
    USE IN ryjbxx
ENDIF
crtvryjbxx(.T.,conn)

SELECT ryjbxx
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
SELECT ryzwbh
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
SET ORDER TO srny IN ryzwbh
SELECT ndkh
INDEX ON khnd+dwbm+grbm TAG ndbm
SELECT jxgz
INDEX ON ny+dwbm+grbm TAG ndbm
SELECT hisbase
INDEX ON dwbm+grbm+jsnf+jsyf+jslb TAG bmlb


CURSORSETPROP("Buffering",5,"ryjbxx")
CURSORSETPROP("Buffering",5,"ryzwbh")
CURSORSETPROP("Buffering",5,"xl")
CURSORSETPROP("Buffering",5,"ndkh")
CURSORSETPROP("Buffering",5,"jxgz")
CURSORSETPROP("Buffering",5,"tgxx")

v_rybm=ryjbxx.dwbm+ryjbxx.grbm

SET FILTER TO dwbm+grbm=v_rybm IN xl
SET FILTER TO dwbm+grbm=v_rybm IN ryzwbh
SET FILTER TO dwbm+grbm=v_rybm IN ndkh
SET FILTER TO dwbm+grbm=v_rybm IN jxgz
SET FILTER TO dwbm+grbm=v_rybm IN hjxx
SET FILTER TO dwbm+grbm=v_rybm IN jx
SET FILTER TO dwbm+grbm=v_rybm IN jdzw
SET FILTER TO dwbm+grbm=v_rybm IN hisbase
SET FILTER TO dwbm+grbm=v_rybm IN tgxx
SET FILTER TO dwbm+grbm=v_rybm IN tgqgz2006