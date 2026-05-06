func gmjy
     sele dwbm
     v_bz=val(tractd(bz,5))-6690679 
     v_bz=allt(str(v_bz))
     if allt(dwbm)=v_bz
        retu .t.
     else
        retu .f.
     endi
     
      