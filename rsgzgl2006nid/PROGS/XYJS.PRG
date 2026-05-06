func xyjs
     PARAMETERS v_ny
     v_nf=VAL(LEFT(v_ny,4))
     v_yf=VAL(right(v_ny,2))
     v_yf=v_yf+1
     IF v_yf=13
        v_yf=1
        v_nf=v_ny+1
     endi   
     v_xy=ALLTRIM(STR(v_nf))+'.'+RIGHT('0'+ALLTRIM(STR(v_yf)),2)
RETURN v_xy
