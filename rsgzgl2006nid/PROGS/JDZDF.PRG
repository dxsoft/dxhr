FUNCTION jdzdf&&返回军队工资相当的地方工资档次

PARAMETERS tcZwbm,tcSrny 

SELECT jdgzbz

   go top
   loca for jsjg=drry.jdzw and tbnd=v_jdtbnd
   v_dc=allt(drry.jdgzdc)
   thisform.pageframe1.page2.c7.text2.value=jdgzbz.a&v_dc   
   sele jzdzdz &&转业军人套入地方职务工资档次  
   seek '01'+drry.jdzw orde tag bmzw
   v_zwmc=dfzw
   sele zwgzbz
   go top
   v_tbnd=thisform.tbnd
   if gzbzdw()  
  v_qsse=gzbz.a1
  v_ce=zwgzbz.a2-zwgzbz.a1 &&计算档差
  v_jdse=thisform.pageframe1.page2.c7.text2.value*0.8   
  x=int((v_jdse-v_qsse)/v_ce) 
  v_dfdc=allt(str(x+1))
  v_dfse=gzbz.a&v_dfdc
  if v_jdse-v_dfse >= v_ce/2
     v_dfdc=allt(str(val(v_dfdc)+1))
  endi
  thisform.pageframe1.page2.c7.text3.value=v_dfdc   
   endi
