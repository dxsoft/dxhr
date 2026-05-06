FUNCTION dfbt
PARAMETERS tcDwbm

m.recn=RECNO("dwbm")
SEEK tcDwbm ORDER dwbm IN dwbm
IF FOUND("dwbm")
    m.retv=dwbm.dfbt
ELSE
    m.retv=0
ENDIF
IF m.recn>0
    GO (m.recn) IN dwbm
ENDIF
RETURN m.retv