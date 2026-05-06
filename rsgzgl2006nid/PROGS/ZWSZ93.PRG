para x1,y1,x2,y2,v_zwmc,v_qt
sele zwgzb
if rytgxx.ryfl='管理人员'
   set filt to (left(lbbm,2)=rytgxx.dwsx or left(lbbm,2)='27') and len(allt(lbbm))>2
else
   set filt to left(lbbm,2)=rytgxx.dwsx and len(allt(lbbm))>2
endi
if type('v_qt')='C' and v_qt='93'
   v_filt=filter()+" or lbbm='999'"
   set filt to &v_filt
endi
go top
if !empt(v_zwmc)
   loca for zwmc=v_zwmc
else
   if left(lbbm,2)='01'
      go bott
   endi   
endi
define windows gzjsgl1 at x1,y1 to x2,y2 in screen close grow float
brow field zwmc nome node noap noed window gzjsgl1 title '职务工资表'
