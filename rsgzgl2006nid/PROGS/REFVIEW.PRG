FUNCTION refview

PARAMETERS tTag,tAll

IF tTag
    DO FORM frminfo WITH '更新视图......' NAME info1
ENDIF

OPEN DATABASE h+"\data\gzjsgl.dbc"

****
IF tAll
	&&&&ndkh
	CREATE SQL VIEW ndkh as SELECT dndkh.*;
	 FROM gzjsgl!dndkh;
	 WHERE Dndkh.dwbm = ?m.pdwbm;
	 ORDER BY Dndkh.dwbm, Dndkh.grbm, Dndkh.khnd

	DBSETPROP("ndkh","view","tables","dndkh")

	DBSETPROP("ndkh.dwbm","field","keyfield",.t.)
	DBSETPROP("ndkh.grbm","field","keyfield",.t.)
	DBSETPROP("ndkh.khnd","field","keyfield",.t.)

	IF !USED("dndkh")
    	USE dndkh IN 0
    ENDIF
	FOR i=1 TO FCOUNT("dndkh")
	    fn="ndkh."+FIELD(i,"dndkh")
	    fn1="dndkh."+FIELD(i,"dndkh")
	    DBSETPROP(fn,"field","updatename",fn1)
	    DBSETPROP(fn,"field","updatable",.t.)
	ENDFOR
	USE IN dndkh

	DBSETPROP("ndkh","view","SendUpdates",.t.)

	&&&&ryzwbh
	CREATE SQL VIEW ryzwbh as SELECT dryzwbh.*;
	 FROM gzjsgl!dryzwbh;
	 WHERE Dryzwbh.dwbm = ?m.pdwbm;
	 ORDER BY Dryzwbh.srny

	DBSETPROP("ryzwbh","view","tables","dryzwbh")

	DBSETPROP("ryzwbh.dwbm","field","keyfield",.t.)
	DBSETPROP("ryzwbh.grbm","field","keyfield",.t.)
	DBSETPROP("ryzwbh.srny","field","keyfield",.t.)

	IF !USED("dryzwbh")
    	USE dryzwbh IN 0
    ENDIF
	FOR i=1 TO FCOUNT("dryzwbh")
	    fn="ryzwbh."+FIELD(i,"dryzwbh")
	    fn1="dryzwbh."+FIELD(i,"dryzwbh")
	    DBSETPROP(fn,"field","updatename",fn1)
	    DBSETPROP(fn,"field","updatable",.t.)
	ENDFOR
	USE IN dryzwbh

	DBSETPROP("ryzwbh","view","SendUpdates",.t.)

	&&&&xl
	CREATE SQL VIEW xl as SELECT dxl.*;
	 FROM gzjsgl!dxl;
	 WHERE Dxl.dwbm = ?m.pdwbm;
	 ORDER BY Dxl.bysj

	DBSETPROP("xl","view","tables","dxl")

	DBSETPROP("xl.dwbm","field","keyfield",.t.)
	DBSETPROP("xl.grbm","field","keyfield",.t.)
	DBSETPROP("xl.xlbm","field","keyfield",.t.)
	DBSETPROP("xl.bysj","field","keyfield",.t.)

	IF !USED("dxl")
    	USE dxl IN 0
    ENDIF
	FOR i=1 TO FCOUNT("dxl")
	    fn="xl."+FIELD(i,"dxl")
	    fn1="dxl."+FIELD(i,"dxl")
	    DBSETPROP(fn,"field","updatename",fn1)
	    DBSETPROP(fn,"field","updatable",.t.)
	ENDFOR
	USE IN dxl

	DBSETPROP("xl","view","SendUpdates",.t.)
ENDIF

**********
CLOSE ALL

COPY FILE &h\data\gzjsgl.dbc to &h\qt\gzjsgl.dbc
COPY FILE &h\data\gzjsgl.dct to &h\qt\gzjsgl.dct
COPY FILE &h\data\gzjsgl.dcx to &h\qt\gzjsgl.dcx

IF tTag
    info1.visible=.F.
    info1.release

    MESSAGEBOX("更新完毕",64,"提示",5)
ENDIF
