FUNCTION crtvhisbaseb

PARAMETERS ttUpdates,tcConn

IF USED("hisbaseb")
    USE IN hisbaseb
ENDIF

SQLEXEC(tcConn,"select *,hj2-hj1 as zze,99 as xh from hisbaseb where dwbm='"+m.pdwbm+"'","hisbaseb")

IF ttUpdates
	CURSORSETPROP("tables","hisbaseb","hisbaseb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,jslb,jsnf,jsyf","hisbaseb")

	fn=""
	FOR i=1 TO FCOUNT("hisbaseb")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
	    	fn=fn+","+FIELD(i,"hisbaseb")
	    ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hisbaseb")

	fn=""
	FOR i=1 TO FCOUNT("hisbaseb")
	    IF UPPER(FIELD(i))<>"ZZE" AND UPPER(FIELD(i))<>"XH"
    	    fn=fn+","+FIELD(i,"hisbaseb")+" hisbaseb."+FIELD(i,"hisbaseb")
    	ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hisbaseb")

	CURSORSETPROP("sendupdates",.t.,"hisbaseb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hisbaseb
INDEX ON dwbm+grbm+jsnf+jsyf+jslb TAG bmlb ADDITIVE
INDEX ON dwbm+grbm+yznf+yzyf+yzjslb TAG yzbm ADDITIVE
INDEX ON xh TAG xh ADDITIVE


FUNCTION crtvryjbxxb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from dryjbxxb where dwbm='"+m.pdwbm+"'","ryjbxxb")

IF ttUpdates
	CURSORSETPROP("tables","dryjbxxb","ryjbxxb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","ryjbxxb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxb")
        fn=fn+","+FIELD(i,"ryjbxxb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryjbxxb")

	fn=""
	FOR i=1 TO FCOUNT("ryjbxxb")
        fn=fn+","+FIELD(i,"ryjbxxb")+" dryjbxxb."+FIELD(i,"ryjbxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryjbxxb")

	CURSORSETPROP("sendupdates",.t.,"ryjbxxb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryjbxxb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm TAG dwbm ADDITIVE


FUNCTION crtvhjxxb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from hjxxb where dwbm='"+m.pdwbm+"'","hjxxb")

IF ttUpdates
	CURSORSETPROP("tables","hjxxb","hjxxb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,hjmc,hjsj","hjxxb")

	fn=""
	FOR i=1 TO FCOUNT("hjxxb")
        fn=fn+","+FIELD(i,"hjxxb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"hjxxb")

	fn=""
	FOR i=1 TO FCOUNT("hjxxb")
        fn=fn+","+FIELD(i,"hjxxb")+" hjxxb."+FIELD(i,"hjxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"hjxxb")

	CURSORSETPROP("sendupdates",.t.,"hjxxb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT hjxxb
INDEX ON dwbm+grbm+tqyjjssj TAG bm addi
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE

FUNCTION crtvjdzwb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from jdzwb where dwbm='"+m.pdwbm+"'","jdzwb")

IF ttUpdates
	CURSORSETPROP("tables","jdzwb","jdzwb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zjbm","jdzwb")

	fn=""
	FOR i=1 TO FCOUNT("jdzwb")
        fn=fn+","+FIELD(i,"jdzwb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jdzwb")

	fn=""
	FOR i=1 TO FCOUNT("jdzwb")
        fn=fn+","+FIELD(i,"jdzwb")+" jdzwb."+FIELD(i,"jdzwb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jdzwb")

	CURSORSETPROP("sendupdates",.t.,"jdzwb")
	CURSORSETPROP("wheretype",2,"jdzwb")
ENDIF

SELECT jdzwb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE


FUNCTION crtvjxb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from jxb where dwbm='"+m.pdwbm+"'","jxb")

IF ttUpdates
	CURSORSETPROP("tables","jxb","jxb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,jx","jxb")

	fn=""
	FOR i=1 TO FCOUNT("jxb")
        fn=fn+","+FIELD(i,"jxb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxb")

	fn=""
	FOR i=1 TO FCOUNT("jxb")
        fn=fn+","+FIELD(i,"jxb")+" jxb."+FIELD(i,"jxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxb")

	CURSORSETPROP("sendupdates",.t.,"jxb")
	CURSORSETPROP("wheretype",2,"jxb")
ENDIF

SELECT jxb
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON dwbm+grbm+jx TAG bmjx ADDITIVE


FUNCTION crtvndkhb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from dndkhb where dwbm='"+m.pdwbm+"'","ndkhb")

IF ttupdate
	CURSORSETPROP("tables","dndkhb","ndkhb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,khnd","ndkhb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhb")
        fn=fn+","+FIELD(i,"ndkhb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ndkhb")

	fn=""
	FOR i=1 TO FCOUNT("ndkhb")
        fn=fn+","+FIELD(i,"ndkhb")+" dndkhb."+FIELD(i,"ndkhb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ndkhb")

	CURSORSETPROP("sendupdates",.t.,"ndkhb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ndkhb
INDEX ON khnd+dwbm+grbm TAG ndbm

FUNCTION crtvqtqkb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select * from qtqkb where dwbm='"+m.pdwbm+"'","qtqkb")

IF ttUpdates
	CURSORSETPROP("tables","qtqkb","qtqkb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,jslb,jsnf,jsyf","qtqkb")

	fn=""
	FOR i=1 TO FCOUNT("qtqkb")
        fn=fn+","+FIELD(i,"qtqkb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"qtqkb")

	fn=""
	FOR i=1 TO FCOUNT("qtqkb")
        fn=fn+","+FIELD(i,"qtqkb")+" qtqkb."+FIELD(i,"qtqkb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"qtqkb")

	CURSORSETPROP("sendupdates",.t.,"qtqkb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT qtqkb
INDEX ON dwbm+grbm+jslb+jsnf+jsyf TAG bmlb ADDITIVE

FUNCTION crtvryzwbhb

PARAMETERS ttUpdate,tcConn

m=SQLEXEC(tcConn,"select * from dryzwbhb where dwbm='"+m.pdwbm+"'","ryzwbhb")

IF ttUpdate
	CURSORSETPROP("tables","dryzwbhb","ryzwbhb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,zwbm,srny","ryzwbhb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhb")
	    fn=fn+","+FIELD(i,"ryzwbhb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ryzwbhb")

	fn=""
	FOR i=1 TO FCOUNT("ryzwbhb")
	    fn=fn+","+FIELD(i,"ryzwbhb")+" dryzwbhb."+FIELD(i,"ryzwbhb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ryzwbhb")

	CURSORSETPROP("sendupdates",.t.,"ryzwbhb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT ryzwbhb
INDEX ON dwbm+grbm+xrzwbz TAG bmbz ADDITIVE
INDEX ON dwbm+grbm+srny TAG bmny ADDITIVE
INDEX ON srny TAG srny ADDITIVE
SET ORDER TO srny IN ryzwbhb

FUNCTION crtvtgxxb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from dtgxxb where dwbm='"+m.pdwbm+"'","tgxxb")

IF ttUpdate
	CURSORSETPROP("tables","dtgxxb","tgxxb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","tgxxb")

	fn=""
	FOR i=1 TO FCOUNT("tgxxb")
        fn=fn+","+FIELD(i,"tgxxb")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgxxb")

	fn=""
	FOR i=1 TO FCOUNT("tgxxb")
        fn=fn+","+FIELD(i,"tgxxb")+" dtgxxb."+FIELD(i,"tgxxb")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgxxb")

	CURSORSETPROP("sendupdates",.t.,"tgxxb")
	CURSORSETPROP("wheretype",2)
ENDIF


FUNCTION crtvxlb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from dxlb where dwbm='"+m.pdwbm+"' order by bysj","xlb")
CURSORSETPROP("tables","dxlb","xlb")
CURSORSETPROP("keyfieldlist","dwbm,grbm,xlbm,bysj","xlb")

fn=""
FOR i=1 TO FCOUNT("xlb")
    fn=fn+","+FIELD(i,"xlb")
ENDFOR

CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"xlb")

fn=""
FOR i=1 TO FCOUNT("xlb")
    fn=fn+","+FIELD(i,"xlb")+" dxlb."+FIELD(i,"xlb")
ENDFOR

CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"xlb")

CURSORSETPROP("sendupdates",.t.,"xlb")
CURSORSETPROP("wheretype",2,"xlb")

SELECT xlb
INDEX ON dwbm+grbm+xl TAG bmxl ADDITIVE



FUNCTION crtvjxgzb

PARAMETERS ttUpdates,tcConn

SQLEXEC(tcConn,"select a.*,b.cjgzny,b.zzny,b.xb,0000 as hj from djxgzb a,dryjbxx b where a.dwbm='"+m.pdwbm+"' and a.ny='"+m.ny+"' and a.dwbm=b.dwbm and a.grbm=b.grbm","jxgzb")

IF ttUpdates
	CURSORSETPROP("tables","djxgzb","jxgzb")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,ny","jxgzb")

	fn=""
	FOR i=1 TO FCOUNT("jxgzb")
	    IF !INLIST(FIELD(i,"jxgzb"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgzb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"jxgzb")

	fn=""
	FOR i=1 TO FCOUNT("jxgzb")
	    IF !INLIST(FIELD(i,"jxgzb"),"CJGZNY","ZZNY","XB","HJ")
            fn=fn+","+FIELD(i,"jxgzb")+" djxgzb."+FIELD(i,"jxgzb")
        ENDIF
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"jxgzb")

	CURSORSETPROP("sendupdates",.t.,"jxgzb")
	CURSORSETPROP("wheretype",2)
ENDIF

SELECT jxgzb
INDEX ON dwbm+grbm+ny TAG bm ADDITIVE

FUNCTION crtvtgqgzb

PARAMETERS ttUpdate,tcConn

SQLEXEC(tcConn,"select * from tgqgz2006b where dwbm='"+m.pdwbm+"'","tgqgz2006b")

IF ttUpdate
	CURSORSETPROP("tables","tgqgz2006b","tgqgz2006b")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","tgqgz2006b")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006b")
	    fn=fn+","+FIELD(i,"tgqgz2006b")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"tgqgz2006b")

	fn=""
	FOR i=1 TO FCOUNT("tgqgz2006b")
	    fn=fn+","+FIELD(i,"tgqgz2006b")+" tgqgz2006b."+FIELD(i,"tgqgz2006b")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"tgqgz2006b")

	CURSORSETPROP("sendupdates",.t.,"tgqgz2006b")
	CURSORSETPROP("wheretype",2)
ENDIF
SELECT tgqgz2006b
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE