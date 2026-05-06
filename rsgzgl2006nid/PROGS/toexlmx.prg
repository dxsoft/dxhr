LPARAMETERS aTable,cTitle,cTemplate,aFldLst,j

PRIVATE filename

SELECT (aTable)
IF empty(cTitle)
    cTitle="调资测算明细表"
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


FldCnt = iif(empty(cTemplate),iif(Empty(aFldLst),fCount()-1,aLen(aFldLst,1)),xlsheet.cells(2,1).value) &&field's count
StartRow = iif(empty(cTemplate),5,xlsheet.cells(2,2).value)&&Start Row

if type("FldCnt")<>"N"
    return
endif

*set the border's style and align mode
RangeString = xlsheet.cells(RecCnt+StartRow-3,FldCnt).address
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

RangeString = "$A$2:"+xlsheet.cells(2,FldCnt).address
xlSheet.range(RangeString).borders(3).LineStyle = 12
RangeString = xlsheet.cells(RecCnt+StartRow-3,1).address+":"+xlsheet.cells(RecCnt+StartRow-3,FldCnt).address
xlSheet.range(RangeString).borders(4).LineStyle = 12
RangeString = "$A$2:"+xlsheet.cells(RecCnt+StartRow-3,1).address
xlSheet.range(RangeString).borders(1).LineStyle = 12
RangeString = xlsheet.cells(2,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-3,FldCnt).address
xlSheet.range(RangeString).borders(2).LineStyle = 12

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
xlSheet.PageSetup.PrintTitleRows="$2:$2"

RangeString = xlsheet.cells(2,FldCnt).address
Rangestring ="$A$2:"+Rangestring
xlSheet.range(RangeString).font.name="黑体"

IF !empty(aFldLst)
	FOR i=1 to FldCnt
        xlSheet.cells(2,i).value = alltrim(aFldLst[i,1])
	ENDFOR
ENDIF

xlApp.visible = .t.
RELEASE xlApp