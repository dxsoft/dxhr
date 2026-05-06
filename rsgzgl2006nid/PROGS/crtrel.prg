FUNCTION crtrel

PARAMETERS v_grbm

IF PCOUNT()=0
    SELECT hisbase
    INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2,5) TAG bmlbhj ADDITIVE
    CURSORSETPROP("Buffering",5,"hisbase")
	SET ORDER TO tag bmlbhj DESCENDING

    GO top

	v_bmbz='no'
	v_id=""

	SCAN
        IF hisbase.dwbm + hisbase.grbm <> LEFT(v_bmbz,14)
			v_bmbz=hisbase.dwbm + hisbase.grbm
			v_id=""
		ENDIF

        REPLACE sid WITH v_id IN hisbase

		v_id=hisbase.id
	    SELECT hisbase
	    SCATTER MEMVAR
	ENDSCAN
    
ELSE
    SELECT hisbase
    INDEX ON dwbm+grbm+jsnf+jsyf+STR(hj2,5) TAG bmlbhj ADDITIVE
    CURSORSETPROP("Buffering",5,"hisbase")
	SET ORDER TO tag bmlbhj DESCENDING

    GO top

	v_bmbz='no'
	v_id=""

	SCAN FOR grbm=v_grbm
        IF hisbase.dwbm + hisbase.grbm <> LEFT(v_bmbz,14)
			v_bmbz=hisbase.dwbm + hisbase.grbm
			v_id=""
		ENDIF

        REPLACE sid WITH v_id IN hisbase

		v_id=hisbase.id
	    SELECT hisbase
	    SCATTER MEMVAR
	ENDSCAN
ENDIF

RETURN TABLEUPDATE(1,.t.,"hisbase")

