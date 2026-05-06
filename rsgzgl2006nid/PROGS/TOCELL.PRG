PARAMETERS v_qsy,v_ys,v_bblb,v_ml,v_table &&接收起始页,页数,报表类别,命令

IF type('v_bblb')<>'C' 
   v_bblb='no'
ENDIF

IF type('v_ml')<>'C' 
    v_ml=''
ENDIF

IF type('v_table')<>'C' 
    v_table='ryjbxx'
ENDIF

SELECT bbdy &&定项目区首行位置
SET FILTER TO cfilename=rptinfo.cfilename
GO top

IF v_bblb<>'spb'
	LOCATE FOR LEFT(jsgs,3)='a03' or left(jsgs,3)='b32' or left(jsgs,3)='t03' or left(jsgs,3)='t30'  
	IF !found()
	    messagebox('表样中找不到数据区!',64,'系统提示')
	    retu
	ENDIF
	
	v_h=hh &&找出起始行列号
    v_L=lh
    FOR j=1 to 20
        LOCATE FOR hh=v_h and lh=j
        IF found()
           v_L=lh 
           exit
        ENDIF
        
    ENDFOR
 
    IF '单行' $ rptinfo.bbys or empt(rptinfo.bbys)
       go top
       loca for hh=v_h+1
       if found()
          messagebox('cell 表样不是单行式!',64,'系统提示')
          return
       endi 
    else
       go top
       loca for hh=v_h+1
       if !found()
          messagebox('cell 表样不是双行式!',64,'系统提示')
          return
       endi 
    endi
endi
v_file=allt(rptinfo.cfilename)+'.cll'
if !file(h+'\reports\'+v_file)
    messagebox('无 cell 表样,请先定义表样!',64,'系统提示')
    return
endi
sele bbdy
go top
if empt(lh)
   messagebox('表样中列定义未完!',64,'系统提示')
   retu
endi

wait window at 25,65 '正在传输数据...' nowait

IF v_bblb='spb'
   v_file1=allt(rptinfo.cfilename)+'spb.cll'
else
   v_file1=allt(rptinfo.cfilename)+'hmc.cll'
endi
copy file &h\reports\&v_file to &h\reports\&v_file1 &&创建文件

*****创建对象****** 
PRIVATE strOpenFileName As String

SET CLASSLIB TO &h\libs\aa_app.vcx

ll=NEWOBJECT("reportsystem")
ll.openfile(h+'\reports\'+v_file1,0)
ll.visible=.t. 
ll.show()
v_yh=0
if v_bblb='spb'
   sele (v_table)
   count to v_rs
   go top
   scan
       v_yh=v_yh+1
       v_rybm=dwbm+grbm
       seek v_rybm+'1' orde tag bmbz in ryzwbh
       seek v_rybm orde tag dwgrbm in rytgxx
       seek v_rybm orde tag dwgrbm in trryxx
       seek v_rybm orde tag dwgrbm in tdqgzb
       
       SELECT bbdy
       SCAN
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
                              if &v_table .jslb='正常职务'
 	                             v_bds=v_bds+allt(jshzdm)+v_ysf
                              endi 
                         case v_jsgs1='b03' or left(v_jsgs1,2)='w1' 
                              if &v_table .jslb='正常级别'
 	                             v_bds=v_bds+allt(jshzdm)+v_ysf
                              endi 
                         otherwise
	                          v_bds=v_bds+allt(jshzdm)+v_ysf
                      endc
	               endi
	            else
	               wait window at 25,65 '报表定义中 '+bbdy.jsgs+' 有误,请检查修改!'timeout 5
                   release ll
	               retu 
	            endi 
	            v_jsgs=right(v_jsgs,len(v_jsgs)-4)
	         endd
	         sele (v_table)
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
                *if bbdy.jsgs='b05' 
                *   v_value='从'+v_value+'执行'
                *endi   
	         endi
             v_sheet=ll.cell1.getcursheet()
             if type('v_value')='C'
	            ll.cell1.s(bbdy.lh,bbdy.hh,allt(v_value))
	         else
	            ll.cell1.d(bbdy.lh,bbdy.hh,round(v_value,1))
	         endi   
             sele bbdy
       ends

       sele (v_table)
       if v_ml='打印'
          ll.cell1.printsheet(0,0)
       else
           v_sheet=ll.cell1.getcursheet()
           v_capt='P'+allt(str(v_yh))
           ll.cell1.setsheetlabel(v_sheet,v_capt)
	       skip 1
	       if eof()
	          go bott
	          exit
	       endi
	       skip -1
	       ll.cell1.appendsheetfromfile(h+'\reports\'+v_file,0,1)
       endi
	   sele (v_table)
   ends
   if v_rs<255
      ll.visible=.t. 
      ll.show()
      ll.cell1.setcursheet(0)
      retu
   endi
   retu
endi
******送报表花名册数据区********
if rptinfo.bbys='双行'
   sele bbdy
   loca for hh=v_h+1
   if !found()
      messagebox('当前表样不是双行,请重新定义!',64,'系统提示')
      retu
   endi  
else
   sele bbdy
   loca for hh=v_h+1
   if found()
      messagebox('当前表样不是单行,请重新定义!',64,'系统提示')
      retu
   endi  
endi

sele (v_table)
go top
do _rddwmc in tocell
SELECT bbdy
SET FILTER TO cfilename=rptinfo.cfilename and hh=v_h
go bott
v_Lmax=lh
dime xj(2,v_Lmax),hj(2,v_Lmax)
xj=0
hj=0
********送花名册数据********
sele (v_table)
count to v_jzxh
if rptinfo.bbys='双行'
   v_col=v_h+2
else
   v_col=v_h+1 &&行号
endi
v_sheet=ll.cell1.getcursheet()
v_height=ll.cell1.getrowheight(0,v_col,v_sheet)
v_font=ll.cell1.getcellfont(v_L,v_col,v_sheet)
v_size=ll.cell1.getcellfontsize(v_L,v_col,v_sheet)

v_yh=1   &&页号
v_xh=0 &&记录序号
go top
v_sheet=ll.cell1.getcursheet()
v_capt='P'+allt(str(v_yh))
ll.cell1.setsheetlabel(v_sheet,v_capt)
scan
    v_xh=v_xh+1
    v_rybm=&v_table .dwbm+&v_table .grbm
    if v_table='ryjbxx'
       seek v_rybm+'1' orde tag bmbz in ryzwbh
       seek v_rybm orde tag dwgrbm in rytgxx
       seek v_rybm orde tag dwgrbm in trryxx
       seek v_rybm orde tag dwgrbm in tdqgzb
    endi   
    do _tocellh in tocell &&送一行数据
    v_col=v_col+1 
     
    sele (v_table)
    
    if v_xh=rptinfo.mrhs*v_yh and at('无小计',rptinfo.bbys)=0 &&判断页尾
       v_yh=v_yh+1
       if rptinfo.bbys='双行'
          v_col=v_h+2
       else
          v_col=v_h+1
       endi 
       skip 1
       if eof()
          go bott
          exit
       endi
       skip -1
       ll.cell1.appendsheetfromfile(h+'\reports\'+v_file,0,1)
       do _rddwmc in tocell
       v_sheet=ll.cell1.getcursheet()
       v_capt='P'+allt(str(v_yh))
       ll.cell1.setsheetlabel(v_sheet,v_capt)
    endi
    v_sheet=ll.cell1.getcursheet()
    v_allh=ll.cell1.getrows(v_sheet)
    if v_col>v_allh-3
       ll.cell1.insertrow(v_allh,1,v_sheet)
    endi   
    sele (v_table)
ends

*****送合计*****
if rptinfo.bbys='单行无小计'
   count to v_rs
   ll.cell1.s(v_L,v_col,'合计')
   ll.cell1.d(v_L+1,v_col,v_rs)
   for j=v_L+2 to v_Lmax
       ll.cell1.setcellfont(j,v_col,v_sheet,v_font)
       ll.cell1.setcellfontsize(j,v_col,v_sheet,v_size)
       ll.cell1.setcellinput(j,v_col,v_sheet,5) 
       if hj(1,j)<>0
          ll.cell1.d(j,v_col,hj(1,j))
       endi
   endf
   v_sheet=ll.cell1.getcursheet()
   ll.cell1.drawgridline(v_L,v_col,v_Lmax,v_col,0,2,-1) &&划表格线
   ll.cell1.setrowheight(0,v_height,v_col,v_sheet)
endi

wait clear
ll.visible=.t. 
ll.show()

SELECT bbdy
SET FILTER TO cfilename=rptinfo.cfilename
release xj,hj
retu   

******读单位名称
proc _rddwmc   
sele bbdy
set filt to cfilename=rptinfo.cfilename

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
           release ll
           retu 
        endi 
        v_jsgs=right(v_jsgs,len(v_jsgs)-4)
     endd
     sele (v_table)
     if right(v_bds,1)=='+'
        v_bds=left(v_bds,len(v_bds)-1)
     endi
     if empt(v_bds)
        v_value=''
     else   
        v_value=evaluate(v_bds) 
     endi   
     v_sheet=ll.cell1.getcursheet()
     ll.cell1.s(bbdy.lh,bbdy.hh,allt(v_value))
     sele bbdy
ends
retu   

********送一列数据*******
proc _tocellh
    v_hjh=1
    xj=0
    sele bbdy  &&传送整列数据
    if '单行' $ rptinfo.bbys or empt(rptinfo.bbys)
       set filt to cfilename=rptinfo.cfilename and hh=v_h
    else
       set filt to cfilename=rptinfo.cfilename and (hh=v_h or hh=v_h+1)
    endi   
    scan
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
              release ll
              sele bbdy
              set filt to cfilename=rptinfo.cfilename
              retu 
           endi 
           v_jsgs=right(v_jsgs,len(v_jsgs)-4)
        endd
        v_sheet=ll.cell1.getcursheet()
        if !empt(v_bds) &&传送一列数据
           if rptinfo.bblb='变动两次'
              sele xzjsgzbb
              seek ryjbxx.dwbm+ryjbxx.grbm+ryjbxx.jsnf+ryjbxx.jsyf+ryjbxx.jslb 
           endi  
           sele (v_table)
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
          ll.cell1.setcellfont(bbdy.lh,v_col,v_sheet,v_font)
          ll.cell1.setcellfontsize(bbdy.lh,v_col,v_sheet,v_size)
          ll.cell1.setcellinput(bbdy.lh,v_col,v_sheet,5) 
          if type('v_value')='C' 
             ll.cell1.s(bbdy.lh,v_col,allt(v_value))
          else
             IF AT('gznx',LOWER(v_bds))>0 or AT('zdgznx',LOWER(v_bds))>0 OR AT('bjglxlnx',LOWER(v_bds))>0
                xj(v_hjh,bbdy.lh)=0
                hj(v_hjh,bbdy.lh)=0
             else
                ll.cell1.setcelldouble(bbdy.lh,v_col,v_sheet,round(v_value,1))
                xj(v_hjh,bbdy.lh)=xj(v_hjh,bbdy.lh)+v_value
                hj(v_hjh,bbdy.lh)=hj(v_hjh,bbdy.lh)+v_value
             endi   
          endi   
        endi &&送完一列
        if bbdy.lh>=v_Lmax and (rptinfo.bbys='双行')
           v_col=v_col+1 
           v_hjh=2
        endi        
        sele bbdy
    ends &&送完一行
    if rptinfo.bbys='单行无小计' 
       v_sheet=ll.cell1.getcursheet()
       ll.cell1.drawgridline(v_L,v_col,v_Lmax,v_col,0,2,-1) &&划表格线
       ll.cell1.setrowheight(0,v_height,v_col,v_sheet)
    endi
retu
