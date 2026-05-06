LPARAMETERS aTable,cTitle,cTemplate,aFldLst,j

PRIVATE filename

SELECT (aTable)
IF empty(cTitle)
    cTitle="调资测算汇总表"
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

if !file(cTemplate) and !empty(cTemplate)
    messagebox('需要的文件未找到。',0,'系统提示')
    return
endif

if !empty(cTemplate)
    xlBook = xlApp.Workbooks.Add(h+"\"+cTemplate)
else
    xlBook = xlApp.Workbooks.Add(h+"\"+filename)
endif

xlSheet = xlBook.Worksheets(1)
xlSheet.rows("1:1").insert()
xlSheet.rows("1:1").insert()


FldCnt = iif(empty(cTemplate),iif(Empty(aFldLst),fCount()-1,aLen(aFldLst,1)),xlsheet.cells(2,1).value) &&field's count
StartRow = iif(empty(cTemplate),5,xlsheet.cells(2,2).value)&&Start Row

if type("FldCnt")<>"N"
    return
endif

*!*	RangeString = "$A$2:"+xlsheet.cells(2,FldCnt).address
*!*	xlSheet.range(RangeString).borders(3).LineStyle = 12
*!*	RangeString = xlsheet.cells(RecCnt+StartRow-3,1).address+":"+xlsheet.cells(RecCnt+StartRow-3,FldCnt).address
*!*	xlSheet.range(RangeString).borders(4).LineStyle = 12
*!*	RangeString = "$A$2:"+xlsheet.cells(RecCnt+StartRow-3,1).address
*!*	xlSheet.range(RangeString).borders(1).LineStyle = 12
*!*	RangeString = xlsheet.cells(2,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-3,FldCnt).address
*!*	xlSheet.range(RangeString).borders(2).LineStyle = 12

*Setting the title
RangeString = xlsheet.cells(1,FldCnt).address
Rangestring ="$A$1:"+Rangestring
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(1,1).value = m.cTitle
xlSheet.cells(1,1).font.size = 18
xlSheet.cells(1,1).font.name = "黑体"
xlSheet.PageSetup.PrintTitleRows="$3:$3"


Rangestring ="$A$2:$A$3"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,1).value = "经费来源"

Rangestring ="$B$2:$B$3"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,2).value = "职务岗位"

Rangestring ="$C$2:$C$3"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,3).value = "人数"

Rangestring ="$D$2:$F$2"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,4).value = "职务工资"

Rangestring ="$G$2:$I$2"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,7).value = "级别工资"

Rangestring ="$J$2:$L$2"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,10).value = "津贴补贴"

Rangestring ="$M$2:$O$2"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,13).value = "月工资合计"

Rangestring ="$P$2:$R$2"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,16).value = "扣减"

Rangestring ="$S$2:$S$3"
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.cells(2,1).value = "实际增资"

xlSheet.range("T:T").clear

*!*	xlSheet.cells(2,1).value = "经费来源"
*!*	xlSheet.cells(2,2).value = "职务岗位"
*!*	xlSheet.cells(2,3).value = "人数"
xlSheet.cells(3,4).value = "变动前"
xlSheet.cells(3,5).value = "变动后"
xlSheet.cells(3,6).value = "增资额"
xlSheet.cells(3,7).value = "变动前"
xlSheet.cells(3,8).value = "变动后"
xlSheet.cells(3,9).value = "增资额"
xlSheet.cells(3,10).value = "变动前"
xlSheet.cells(3,11).value = "变动后"
xlSheet.cells(3,12).value = "增资额"
xlSheet.cells(3,13).value = "变动前"
xlSheet.cells(3,14).value = "变动后"
xlSheet.cells(3,15).value = "增资额"
xlSheet.cells(3,16).value = "养老保险"
xlSheet.cells(3,17).value = "职业年金"
xlSheet.cells(3,18).value = "合计扣款"
xlSheet.cells(2,19).value = "实际增资"

*set the border's style and align mode
RangeString = xlsheet.cells(RecCnt+StartRow-2,FldCnt).address
RangeString ="$A$2:"+ RangeString
xlSheet.range(RangeString).borders(1).LineStyle = 7
xlSheet.range(RangeString).borders(2).LineStyle = 7
xlSheet.range(RangeString).borders(3).LineStyle = 7
xlSheet.range(RangeString).borders(4).LineStyle = 7
*xlSheet.range(RangeString).HorizontalAlignment = 3
xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.range(RangeString).Rowheight=14.25
xlSheet.range(RangeString).NumberFormatLocal = "@"
*xlSheet.columns(1).NumberFormatLocal = "@"
xlSheet.range(RangeString).font.name = "宋体"
xlSheet.range(RangeString).font.size = 9

xlApp.visible = .t.
RELEASE xlApp