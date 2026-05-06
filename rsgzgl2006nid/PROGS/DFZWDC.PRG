FUNCTION dfdc&&计算军队工资相当的地方职务档次

PARAMETERS tcTbnd,tcZwbm,tcZwgzdc,tcZysj

v_jdtbnd=tbnd(tcZysj,"jdgzbz")
GO TOP IN jdgzbz

v_dc=ALLTRIM(tcZwgzdc)
SEEK v_jdtbnd+tcZwbm ORDER tag ndbm IN jdgzbz
v_jdgzse=jdgzbz.a&v_dc

v_tbnd=tbnd(tcZysj,"zwgzbz")
SEEK v_tbnd+tcZwbm ORDER tag ndbm IN zwgzbz
v_qsse=zwgzbz.a1
v_ce=zwgzbz.a2-zwgzbz.a1 &&计算档差

v_jdse=v_jdgzse*0.8   
x=INT((v_jdse-v_qsse)/v_ce) 
v_dfdc=ALLTRIM(STR(x+1))
v_dfse=zwgzbz.a&v_dfdc

IF v_jdse-v_dfse >= v_ce/2
    v_dfdc=ALLTRIM(STR(VAL(v_dfdc)+1))
ENDIF

RETURN v_dfdc