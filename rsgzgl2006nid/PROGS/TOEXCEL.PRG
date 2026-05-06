para v_qsy,v_ys,v_ml,v_bblb &&接收起始页,页数,命令
if type('v_bblb')<>'C' 
   v_bblb='no'
endi
sele bbdy &&定项目区首行位置
set filt to bbdm=bbmc.bbdm
if v_bblb<>'spb'
	loca for left(jsgs,3)='a03'
	if !found()
	   messagebox('表样中找不到 姓名 区!',64,'系统提示')
	   retu
	endi
	v_h=hh
	v_L=lh
    if bbmc.bbys='单行'
       go top
       loca for hh=v_h+1
       if found()
          messagebox('Excel 表样不是单行式或未读取 Excel 表参数定义!',64,'系统提示')
          return
       endi 
       v_file=allt(bbmc.bbdm)+'.xls'
    else
       go top
       loca for hh=v_h+1
       if !found()
          messagebox('Excel 表样不是双行式或未读取 Excel 表参数定义!',64,'系统提示')
          return
       endi 
       v_file=allt(bbmc.bbdm)+'sh.xls'
    endi
else
    v_file=allt(bbmc.bbdm)+'.xls'
endi

if !file(h+'\reports\'+v_file)
    messagebox('无 Excel 表样,请先定义表样!',64,'系统提示')
    return
endi
sele bbdy
go top
if empt(lh)
   messagebox('表样中列定义未完!',64,'系统提示')
   retu
endi

wait window at 25,65 '正在传输数据...' nowait
if v_bblb='spb'
   v_file1=allt(bbmc.bbdm)+'spb.xls'
else
   v_file1=allt(bbmc.bbdm)+'hmc.xls'
endi
   copy file &h\reports\&v_file to &h\reports\&v_file1
   if error()=1102
      myfile=getobject(h+'\reports\'+v_file1) 
      myfile.close
      copy file &h\reports\&v_file to &h\reports\&v_file1
   endi
   v_file=v_file1
local myexcel,mysheet
myexcel= getObject('',"Excel.Application")
*myexcel= CreateObject("Excel.Application")
if error()=1733
    messagebox('该功能需要Excel支持，请安装!',64,'系统提示')
    return
endif
myexcel.workbooks.close
myexcel.workbooks.open(h+'\reports\'+v_file)
mysheet=myexcel.activesheet

if v_bblb='spb'
   sele ryjbxx
   scan
       v_rybm=ryjbxx.dwbm+ryjbxx.grbm
       seek v_rybm+'1' orde tag bmbz in ryzwbh
       seek v_rybm orde tag dwgrbm in rytgxx
       seek v_rybm orde tag dwgrbm in trryxx
       seek v_rybm orde tag dwgrbm in tdqgzb
       sele bbdy
       scan
	         v_jsgs=allt(bbdy.jsgs) 
	         v_bds=''
	         do while !empt(v_jsgs) &&计算出一列的公式
	            v_jsgs1=left(v_jsgs,3) 
	            v_ysf=subs(v_jsgs,4,1)
	            sele gzxmdm
	            loca for jsqzddm=v_jsgs1 or jshzddm=v_jsgs1
	            if found()
	               if left(v_jsgs1,1)='a'
	                  v_bds=v_bds+allt(jsqzdm)+v_ysf
	               else   
                      do case
                         case v_jsgs1='b01' or v_jsgs1='b02'
                              if ryjbxx.jslb='正常职务'
 	                             v_bds=v_bds+allt(jshzdm)+v_ysf
                              endi 
                         case v_jsgs1='b03' or left(v_jsgs1,2)='w1' 
                              if ryjbxx.jslb='正常级别'
 	                             v_bds=v_bds+allt(jshzdm)+v_ysf
                              endi 
                         otherwise
	                          v_bds=v_bds+allt(jshzdm)+v_ysf
                      endc
	               endi
	            else
	               wait window at 25,65 '报表定义中 '+bbdy.jsgs+' 有误,请检查修改!'timeout 5
	               myexcel.quit
	               retu 
	            endi 
	            v_jsgs=right(v_jsgs,len(v_jsgs)-4)
	         endd
	         sele ryjbxx
             if right(v_bds,1)=='+'
                v_bds=left(v_bds,len(v_bds)-1)
             endi
             if empt(v_bds)
                v_value=''
             else   
                v_value=evaluate(v_bds) 
             endi   
	         if type('v_value')='N'
	            if v_value=0
	               v_value=''
	            endi 
             else
                if bbdy.jsgs='b05' 
                   v_value='从'+v_value+'执行'
                endi   
	         endi
	         mysheet.cells(bbdy.hh,bbdy.lh).value=v_value
             sele bbdy
       ends
	   if v_ml='预览'
	      myexcel.visible=.t. 
	      exit
	   else
	      myexcel.activeworkbook.printout(1,1,1,.f.) 
	   endi
	   sele ryjbxx
   ends
   if v_ml='打印'
	  myexcel.quit 
   endi
   retu
endi

******送报表花名册数据区********
sele bbdy
scan for hh<v_h-2 &&送单位名称等
     v_jsgs=allt(bbdy.jsgs) 
     v_bds=''
     do while !empt(v_jsgs) &&计算出一列的公式
        v_jsgs1=left(v_jsgs,3) 
        v_ysf=subs(v_jsgs,4,1)
        sele gzxmdm
        loca for jsqzddm=v_jsgs1 or jshzddm=v_jsgs1
        if found()
           v_bds=v_bds+allt(jshzdm)+v_ysf
        else
           wait window at 25,65 '报表定义中 '+bbdy.jsgs+' 有误,请检查修改!'timeout 5
           myexcel.quit
           retu 
        endi 
        v_jsgs=right(v_jsgs,len(v_jsgs)-4)
     endd
     sele ryjbxx
     if right(v_bds,1)=='+'
        v_bds=left(v_bds,len(v_bds)-1)
     endi
     if empt(v_bds)
        v_value=''
     else   
        v_value=evaluate(v_bds) 
     endi   
     mysheet.cells(bbdy.hh,bbdy.lh).value=v_value
     sele bbdy
ends
if bbmc.bbys='单行'  &&送边线
   v_hmax=v_h-1+bbmc.mrhs
else
   v_hmax=v_h-1+2*bbmc.mrhs 
endi
sele bbdy
set filt to bbdm=bbmc.bbdm and hh=v_h
go bott
v_Lmax=lh
v_star=mysheet.cells(v_h,v_L).address
v_string=mysheet.cells(v_hmax,v_Lmax).address
RangeString =v_star+":"+ v_String
mySheet.range(RangeString).borders(1).LineStyle = 7
mySheet.range(RangeString).borders(2).LineStyle = 7
mySheet.range(RangeString).borders(3).LineStyle = 7
mySheet.range(RangeString).borders(4).LineStyle = 7

********将表样清零********
v_col=v_h &&行号
v_row=v_L   &&列号 
v_xh=0      &&记录序号
v_qsxh=1 && 起始序号
if bbmc.bbys='单行'  
   v_jzxh=bbmc.mrhs && 截止序号
else
   v_jzxh=2*bbmc.mrhs && 截止序号
endi
&&v_L1 为列数
for v_col=v_h to v_h+v_jzxh &&行循环
    for v_row=v_L to v_Lmax
        mysheet.cells(v_col,v_row).value=''
    endfor
endfor    
********送数据********
sele ryjbxx
count to v_jzxh
v_col=v_h &&行号
v_row=v_L   &&列号 
v_qsxh=(v_qsy-1)*bbmc.mrhs+1 && 起始序号
v_yh=v_qsy  &&页号
v_xh=v_qsxh-1 &&记录序号
go top
scan
    v_xh=v_xh+1
    if v_xh<v_qsxh
       loop
    endi     
    v_rybm=ryjbxx.dwbm+ryjbxx.grbm
    seek v_rybm+'1' orde tag bmbz in ryzwbh
    seek v_rybm orde tag dwgrbm in rytgxx
    seek v_rybm orde tag dwgrbm in trryxx
    seek v_rybm orde tag dwgrbm in tdqgzb
    sele bbdy  &&传送整列数据
    if bbmc.bbys='单行'
       set filt to bbdm=bbmc.bbdm and hh=v_h
    else
       set filt to bbdm=bbmc.bbdm and (hh=v_h or hh=v_h+1)
    endi   
    scan
        v_row=lh
        v_jsgs=allt(jsgs) 
        v_bds=''
        do while !empt(v_jsgs) &&计算出一列的公式
           v_jsgs1=left(v_jsgs,3) 
           v_ysf=subs(v_jsgs,4,1)
           sele gzxmdm
           loca for jsqzddm=v_jsgs1 or jshzddm=v_jsgs1
           if found()
              if left(v_jsgs1,1)='a'
                 v_bds=v_bds+allt(jsqzdm)+v_ysf
              else   
                 v_bds=v_bds+allt(jshzdm)+v_ysf
              endi
           else
              wait window at 25,65 '报表定义中第'+str(bbdy.lh)+'列有误,请检查修改!'timeout 5
              myexcel.quit
              sele bbdy
              set filt to bbdm=bbmc.bbdm
              retu 
           endi 
           v_jsgs=right(v_jsgs,len(v_jsgs)-4)
        endd
        if !empt(v_bds) &&传送一列数据
           if bbmc.bblb='变动两次' and betw(bbdy.lh,val(left(bbmc.dycfw,2)),val(right(bbmc.dycfw,2)))
              sele xzjsgzbb
              seek ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb 
              if found() and jslb=bbmc.dyclb
                 v_value=&v_bds
                 if type('v_value')='N'
                    if v_value=0
                       v_value=''
                    endi 
                 endi
                 mysheet.cells(v_col,v_row).value=&v_bds
              else   
                 mysheet.cells(v_col,v_row).value=''
              endi
           else  
             sele ryjbxx
             if right(v_bds,1)=='+'
                v_bds=left(v_bds,len(v_bds)-1)
             endi
             if empt(v_bds)
                v_value=''
             else   
                v_value=evaluate(v_bds) 
             endi   

             if type('v_value')='N'
                if v_value=0
                   v_value=''
                endi 
             endi
             mysheet.cells(v_col,v_row).value=v_value
           endi  
        endi 
        if v_row>=v_Lmax and (bbmc.bbys='双行' or empt(bbmc.bbys))
           v_col=v_col+1 
        endi        
        sele bbdy
    ends
    v_col=v_col+1 
    if v_xh=v_jzxh
       if v_ml='打印'
          myexcel.activeworkbook.printout(1,1,1,.f.) 
       endi   
       exit
    endi   
     
    if v_xh=bbmc.mrhs*v_yh
       if v_ml='打印'
          myexcel.activeworkbook.printout(1,1,1,.f.) 
       endi   
       v_yh=v_yh+1
       v_col=v_h
    endi
    sele ryjbxx
ends
wait clear
if v_ml='预览'
   myexcel.visible=.t. 
else
   myexcel.quit 
endi
sele bbdy
set filt to bbdm=bbmc.bbdm
   