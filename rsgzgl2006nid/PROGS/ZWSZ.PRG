para x1,y1,x2,y2,v_zwmc,v_qt
sele zwgzb
if ryjbxx.ryfl='管理人员'
   do case 
      case x1=1 and (MOD(y1,10))=1
           set filt to (left(lbbm,2)='27' and len(allt(lbbm))>2) &&管理人员行政职务
      case x1=2 and (MOD(y1,10))=2
           set filt to (left(lbbm,2)=ryjbxx.dwsx and len(allt(lbbm))>2) &&管理人员技术职务
      otherwise
           set filt to (left(lbbm,2)=ryjbxx.dwsx or left(lbbm,2)='27') and len(allt(lbbm))>2
   endc 
else
   set filt to left(lbbm,2)=ryjbxx.dwsx and len(allt(lbbm))>2
endi
if type('v_qt')='C' and v_qt='93'
   v_filt=filter()+" or LEFT(lbbm,2)='99'"
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
