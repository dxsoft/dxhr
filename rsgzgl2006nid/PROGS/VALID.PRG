loca for gznx<0 or gznx>60
if found()
   messagebox(xm+grbm+'的工作年限有误,请立即修改!',64,'系统提示') 
   retu
endi
****
   sele jzdzdz
   loca for wz=trryxx.jdzw
   if found()
      v_dfzw=dfzw
      sele ryzwbh
      loca for xzzw=v_dfzw
      if !found()
         messagebox('最后一次职务未录入,无法定资。'+CHR(13)+CHR(10)+CHR(13)+CHR(10)+'请在[人员信息维护]中核对其职务变化情况!',16,'警告!')
         retu
      endi
   else 
      messagebox('转业时军队职务有误!',16,'警告!')
      retu
   endi 
****
FOR j=v_qsnd to v_jcnd &&送年度考核
    sele ndkh
    seek allt(str(j))+v_rybm orde tag ndbm
    if !found() and j<v_jcnd
       messagebox('抱歉！'+trdztmp.xm+'的年度考核结果未输,不能定资,请输该年度考核结果！',64,'系统提示')
       retu
    endi
    insert into trdztmp1 (khnd,khjg) value (allt(str(j)),ndkh.khjg1)
endf
****