LPARAMETERS aTable,cTitle,cTemplate,aFldLst,j

PRIVATE filename

SELECT (aTable)
IF empty(cTitle)
    cTitle="查询结果"
ELSE
    cTitle=alltrim(cTitle)
ENDIF

cExpr=""
IF !empty(aFldLst)
    for i=1 to aLen(aFldLst,1)
	    cExpr=cExpr+alltrim(aFldLst[i,j])+","    
	endfor

	cExpr=left(cExpr,len(cExpr)-1)
ENDIF

filename="qt\"+ctitle+".xls"
if !empty(cExpr)
    copy to (filename) field &cExpr type xl5
else
    copy to (filename) type xl5
endif

count to RecCnt  &&Record count
go top

if eof()
    return
endif

**Create and set the excel's style

xlApp = CreateObject("Excel.Application")
if error()=1733
    messagebox('该功能需要Excel支持，请安装。',0,'系统提示')
    return
endif
xlapp.StandardFontSize = "9"
xlapp.SheetsInNewWorkbook = 1

wait "导出数据......." windows at 60,50  nowait

if !file(H+"\"+cTemplate) and !empty(cTemplate)
    messagebox('需要的文件未找到。',0,'系统提示')
    return
endif

if !empty(cTemplate)
    xlBook = xlApp.Workbooks.Add(h+"\"+cTemplate)
	xlSheet = xlBook.Worksheets(1)
*!*		FldCnt = xlsheet.cells(1,1).value &&field's count
*!*		StartRow = xlsheet.cells(1,2).value-1&&Start Row
*!*		xlSheet.rows("1:1").delete()
else
    xlBook = xlApp.Workbooks.Add(h+"\"+filename)
	xlSheet = xlBook.Worksheets(1)
	xlSheet.rows("1:1").insert()
	FldCnt = iif(Empty(aFldLst),fCount()-1,aLen(aFldLst,1))
	StartRow = 5
endif

*!*	if type("FldCnt")<>"N"
*!*	    return
*!*	endif

*!*	*Setting the title
*!*	RangeString = xlsheet.cells(1,FldCnt).address
*!*	Rangestring ="$A$1:"+Rangestring
*!*	*!*	xlSheet.range(RangeString).select
*!*	*!*	xlSheet.range(RangeString).clear
*!*	*!*	xlSheet.range(RangeString).merge
*!*	xlSheet.cells(1,1).value = m.cTitle
*!*	*!*	xlSheet.cells(1,1).font.size = 18
*!*	*!*	xlSheet.cells(1,1).font.name = "黑体"
*!*	xlSheet.PageSetup.PrintTitleRows="$1:$"+ALLTRIM(STR(StartRow-1))
*!*	*!*	xlSheet.cells(1,1).HorizontalAlignment = 3
*!*	xlSheet.cells(2,3).value=ALLTRIM(dwmc(m.pdwbm))

*!*	RangeString = xlsheet.cells(2,FldCnt).address
*!*	Rangestring ="$A$2:"+Rangestring
*!*	*!*	xlSheet.range(RangeString).font.name="黑体"

*!*	SELECT (aTable)
*!*	k=0
*!*	SCAN
*!*	    xlSheet.cells(StartRow+k,1).value = (k+1+1)/2
*!*		FOR i=1 to FldCnt-1
*!*		    mm=alltrim(FIELD(i))
*!*		    IF !EMPTY(mm)
*!*			    IF TYPE(mm)="N"
*!*			        IF &mm =0
*!*			            LOOP
*!*			        ENDIF
*!*			    ENDIF

*!*	            xlSheet.cells(StartRow+k,i+1).value = &mm
*!*	        ENDIF
*!*		ENDFOR

*!*	    IF INT(k/2)=k/2
*!*			Rangestring ="$A$"+ALLTRIM(STR(StartRow+k))+":"+"$A$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$B$"+ALLTRIM(STR(StartRow+k))+":"+"$B$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$C$"+ALLTRIM(STR(StartRow+k))+":"+"$C$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$V$"+ALLTRIM(STR(StartRow+k))+":"+"$V$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*			Rangestring ="$W$"+ALLTRIM(STR(StartRow+k))+":"+"$W$"+ALLTRIM(STR(StartRow+k+1))
*!*			xlSheet.range(RangeString).merge
*!*	    ENDIF

*!*	    k=k+1
*!*	ENDSCAN
*!*	Rangestring ="$A$"+ALLTRIM(STR(StartRow+k-2))+":"+"$C$"+ALLTRIM(STR(StartRow+k-1))
*!*	xlSheet.range(RangeString).select
*!*	xlSheet.range(RangeString).clear
*!*	xlSheet.range(RangeString).merge
*!*	xlSheet.cells(StartRow+k-2,1).value = "合计"
*!*	xlSheet.cells(StartRow+k-2,1).HorizontalAlignment = 3
*!*	xlSheet.cells(StartRow+k-2,1).VerticalAlignment = 2
*!*	FOR i=0 to FldCnt
*!*	    xlSheet.cells(StartRow+k-2,i+1).font.name = "黑体"
*!*	    xlSheet.cells(StartRow+k-2,i+1).font.bold = .t.
*!*	    xlSheet.cells(StartRow+k-1,i+1).font.name = "黑体"
*!*	    xlSheet.cells(StartRow+k-1,i+1).font.bold = .t.
*!*	ENDFOR
*!*		
*!*	*set the border's style and align mode
*!*	RangeString = xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	RangeString ="$A$3:"+ RangeString
*!*	xlSheet.range(RangeString).borders(1).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(2).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(3).LineStyle = 7
*!*	xlSheet.range(RangeString).borders(4).LineStyle = 7
*!*	*xlSheet.range(RangeString).HorizontalAlignment = 3
*!*	*!*	xlSheet.range(RangeString).VerticalAlignment = 2
*!*	xlSheet.range("$A$"+ALLTRIM(STR(StartRow))+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address).Rowheight=14.25
*!*	xlSheet.range(RangeString).NumberFormatLocal = "@"
*!*	*xlSheet.columns(1).NumberFormatLocal = "@"
*!*	xlSheet.range(RangeString).font.name = "宋体"
*!*	xlSheet.range(RangeString).font.size = 9

*!*	RangeString = "$A$3:"+xlsheet.cells(3,FldCnt).address
*!*	xlSheet.range(RangeString).borders(3).LineStyle = 7&&12
*!*	RangeString = xlsheet.cells(RecCnt+StartRow-1,1).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	xlSheet.range(RangeString).borders(4).LineStyle = 7&&12
*!*	RangeString = "$A$3:"+xlsheet.cells(RecCnt+StartRow-1,1).address
*!*	xlSheet.range(RangeString).borders(1).LineStyle = 7&&12
*!*	RangeString = xlsheet.cells(3,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*	xlSheet.range(RangeString).borders(2).LineStyle = 7&&12

*!*	xlSheet.name=ALLTRIM(dwmc(m.pdwbm))





xlSheet.cells(2,2).value = cTitle



xlSheet.cells(5,4).value = xm

xlSheet.cells(5,7).value = xb
xlSheet.cells(5,7).value = xb
xlSheet.cells(5,9).value = csny
xlSheet.cells(5,12).value = ALLTRIM(subst(xl(dwbm+grbm,jsnf+jsyf),9))
xlSheet.cells(6,4).value = ALLTRIM(dwmc)
xlSheet.cells(6,9).value = cjgzny
xlSheet.cells(6,12).value = gznx
xlSheet.cells(7,4).value = xrzw
xlSheet.cells(7,12).value = srny
xlSheet.cells(8,12).value = xckhndzw
xlSheet.cells(9,5).value = ALLTRIM(zwgw1)
xlSheet.cells(9,7).value = ALLTRIM(zwgw2)
xlSheet.cells(9,9).value = ''
xlSheet.cells(9,12).value = xckhndjb

xlSheet.cells(10,5).value = prtv(iif(at("FF",zwbm1)>0,"",iif(inlist(left(zwbm1,2),"01","02","03","04","21","22","23","24","25"),dtc(jbgzjb1)+"级","")+allt(zwgzdc1)+iif(inlist(left(zwbm1,2),"05","06","07","08","09","10"),"级","档")+iif(!empty(djc1),"倒"+alltr(djc1),"")))

xlSheet.cells(10,7).value = prtv(iif(at("FF",zwbm2)>0,"",iif(inlist(left(zwbm2,2),"01","02","03","04","21","22","23","24","25"),dtc(jbgzjb2)+"级","")+allt(zwgzdc2)+iif(inlist(left(zwbm2,2),"05","06","07","08","09","10"),"级","档")+iif(!empty(djc2),"倒"+alltr(djc2),"")))

xlSheet.cells(11,5).value = prtv(zwgzse1)
xlSheet.cells(11,7).value = prtv(zwgzse2)
xlSheet.cells(11,9).value = prtv(zwgzse2-zwgzse1)



xlSheet.cells(12,5).value = prtv(jbgzse1)
xlSheet.cells(12,7).value = prtv(jbgzse2)
xlSheet.cells(12,9).value = prtv(jbgzse2-jbgzse1)

xlSheet.cells(13,5).value = prtv(jsdjgz1)
xlSheet.cells(13,7).value = prtv(jsdjgz2)
xlSheet.cells(13,9).value = prtv(jsdjgz2-jsdjgz1)

xlSheet.cells(14,5).value = prtv(jsfszwtg1)
xlSheet.cells(14,7).value = prtv(jsfszwtg2)
xlSheet.cells(14,9).value = prtv(jsfszwtg2-jsfszwtg1)

xlSheet.cells(15,5).value = prtv(jhljt1)
xlSheet.cells(15,7).value = prtv(jhljt)
xlSheet.cells(15,9).value = prtv(jhljt-jhljt1)

xlSheet.cells(16,5).value = prtv(blfb1)
xlSheet.cells(16,7).value = prtv(blfb2)
xlSheet.cells(16,9).value = prtv(blfb2-blfb1)

xlSheet.cells(17,5).value = prtv(jjjy1)
xlSheet.cells(17,7).value = prtv(jjjy2)
xlSheet.cells(17,9).value = prtv(jjjy2-jjjy1)

xlSheet.cells(18,5).value = prtv(zwjt1)
xlSheet.cells(18,7).value = prtv(zwjt)
xlSheet.cells(18,9).value = prtv(zwjt-zwjt1)

xlSheet.cells(19,5).value = prtv(dfbt1)
xlSheet.cells(19,7).value = prtv(dfbt2)
xlSheet.cells(19,9).value = prtv(dfbt2-dfbt1)

xlSheet.cells(20,5).value = prtv(jxjt1)
xlSheet.cells(20,7).value = prtv(jxjt)
xlSheet.cells(20,9).value = prtv(jxjt-jxjt1)

xlSheet.cells(21,5).value = prtv(fdgz1)
xlSheet.cells(21,7).value = prtv(fdgz2)
xlSheet.cells(21,9).value = prtv(fdgz2-fdgz1)

xlSheet.cells(22,5).value = prtv(gwjt1)
xlSheet.cells(22,7).value = prtv(gwjt2)
xlSheet.cells(22,9).value = prtv(gwjt2-gwjt1)

xlSheet.cells(23,5).value = prtv(tgblbf1)
xlSheet.cells(23,7).value = prtv(tgblbf)
xlSheet.cells(23,9).value = prtv(tgblbf-tgblbf1)

xlSheet.cells(24,5).value = prtv(njbt1)
xlSheet.cells(24,7).value = prtv(njbt)
xlSheet.cells(24,9).value = prtv(njbt-njbt1)

xlSheet.cells(25,5).value = prtv(qtbt1)
xlSheet.cells(25,7).value = prtv(qtbt)
xlSheet.cells(25,9).value = prtv(qtbt-qtbt1)

xlSheet.cells(26,5).value = prtv(pgbc1)
xlSheet.cells(26,7).value = prtv(pgbc)
xlSheet.cells(26,9).value = prtv(pgbc-pgbc1)

xlSheet.cells(27,5).value = prtv(hj1)
xlSheet.cells(27,7).value = prtv(hj2)
xlSheet.cells(27,9).value = prtv(hj2-hj1)

xlSheet.cells(28,5).value = prtv(jtbl1)
xlSheet.cells(28,7).value = prtv(jxbl(dwbm))

andkhjg(dwbm+grbm,jsnf,@landkh)

yy=gzbdyy(jslb,dwsx)
IF LENC(yy)>9
  bdyy=CHR(10)+CHR(13)+" "+LEFTC(yy,9)+CHR(10)+CHR(13)+' '++SUBSTR(yy,19)+CHR(10)+CHR(13)+" 执行时间："+CHR(10)+CHR(13)+SPACE(8)+jsnf+"年"+jsyf+"月"+CHR(10)+CHR(13)+CHR(10)+CHR(13)
ELSE
  bdyy=CHR(10)+CHR(13)+" "+yy+CHR(10)+CHR(13)+" 执行时间："+CHR(10)+CHR(13)+SPACE(8)+jsnf+"年"+jsyf+"月"+CHR(10)+CHR(13)+CHR(10)+CHR(13)
ENDIF

IF jslb = "正常级别" or jslb="正常档次" or jslb="正常薪级" or jslb="警务套改"
    bdyy=bdyy+" "+iif(jslb = "正常级别" or jslb = "正常档次" ,ALLTRIM(landkh[1,1]) + '年: '+landkh[1,2],'')+CHR(10)+CHR(13)
ENDIF

IF jslb = "正常级别" or (jslb="正常档次" and !inlist(left(zwbm2,2),"08","09","10"))
    bdyy=bdyy+" "+iif(jslb = "正常级别" or jslb = "正常档次" ,ALLTRIM(landkh[2,1]) + '年: '+landkh[2,2],'')+CHR(10)+CHR(13)
ENDIF

bdyy=bdyy+" "+iif(jslb = "正常级别" ,ALLTRIM(landkh[3,1]) + '年: '+landkh[3,2],'')+CHR(10)+CHR(13)

bdyy=bdyy+" "+iif(jslb = "正常级别" ,ALLTRIM(landkh[4,1]) + '年: '+landkh[3,2],'')+CHR(10)+CHR(13)

bdyy=bdyy+" "+iif(jslb = "正常级别" ,ALLTRIM(landkh[5,1]) + '年: '+landkh[3,2],'')+CHR(10)+CHR(13)

xlSheet.cells(12,11).value = bdyy

xlApp.visible = .t.
RELEASE xlApp
