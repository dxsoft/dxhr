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
sele (b_name)
set orde to tag xmbms
go bott
r_num=val(right(xmbm,2))
go top
SET RELA TO xmbm INTO (G_NAME)
F_NUM=FCOUNT() &&字段数 
if b_name='cslrtmp'
   f_num=3
endi    
DIME TMP_A(R_NUM,f_num-(b_ksf-1))
tmp_a=0.00
SCAN FOR &G_NAME .jscx=0
  FOR L=b_ksf TO f_num
      F_NAME=FIELD(L)
      hc=right(xmbm,2)
      TMP_A(VAL(hc),L-(b_ksf-1))=&F_NAME
  ENDFOR
ENDSCAN
FOR J=1 TO v_mjscx  &&支持加减法
    SCAN FOR &G_NAME .jscx=J 
    FOR L=B_KSF TO F_NUM
      K=L-(b_ksf-1) 
      M_GS=ALLTRIM(&G_NAME->jsgs)
      m_gs0='tmp_a('+left(m_gs,2)+','+iif(k>9,allt(str(k)),'0'+allt(str(K)))+')'
      m_gs=RIGHT(M_GS,LEN(M_GS)-2)
      DO WHILE !EMPTY(M_GS)
         M_GS0=M_GS0+left(m_gs,1)+'TMP_A('+subs(m_gs,2,2)+','+iif(k>9,allt(str(k)),'0'+allt(str(K)))+')'
         M_GS=RIGHT(M_GS,LEN(M_GS)-3)
      ENDDO
      F_NAME=FIELD(L)
      REPL &F_NAME WITH eval(M_GS0)
      hc=right(xmbm,2)
      TMP_A(VAL(hc),K)=&F_NAME
    ENDFOR
    ENDSCAN
ENDFOR
release tem_a
sele (b_name)
scan for &g_name .bfb<>0
     v_bfb=&g_name .bfb
     repl je with je*v_bfb/100 
ends
set rela to
RETU
