PARA B_KSF,G_NAME,B_NAME
IF USED(G_NAME)
   SELE (G_NAME)
ELSE   
   SELE 0
   use (g_name)
ENDIF
set orde to tag jscx
go bott
v_mjscx=jscx
set orde to tag xmbms
go bott
h_num=val(right(xmbm,2))

sele &b_name
h_zds=fcount()
DIME TMP_A(H_NUM)
set filt to left(rybm,2)=v_dwbm
scan
  tmp_a=0.00
  FOR L=b_ksf TO h_zds
    F_NAME=FIELD(L)
    hc=right(f_name,2)
    TMP_A(VAL(hc))=&F_NAME
  ENDFOR
  FOR J=1 TO v_mjscx  &&支持加减法
    sele &g_name
    SCAN FOR jscx=J 
         M_GS=ALLT(jsgs)
         m_gs0='tmp_a('+left(m_gs,2)+')'
         m_gs=RIGHT(M_GS,LEN(M_GS)-2)
         DO WHILE !EMPTY(M_GS)
            M_GS0=M_GS0+left(m_gs,1)+'TMP_A('+subs(m_gs,2,2)+')'
            M_GS=RIGHT(M_GS,LEN(M_GS)-3)
         ENDDO
         F_NAME=xmbm
         hc=right(xmbm,2)
         sele &b_name
         REPL &F_NAME WITH eval(M_GS0)
         TMP_A(VAL(hc))=&F_NAME
         sele &g_name
    ENDSCAN
  ENDFOR
  sele &b_name
ends
release tem_a
sele (g_name)
scan for bfb<>0
     v_bfb=bfb
     v_name=xmbm
     sele &b_name
     repl all &v_name with &v_name*v_bfb/100 
     sele &g_name
ends
sele &b_name
set filt to
RETU
