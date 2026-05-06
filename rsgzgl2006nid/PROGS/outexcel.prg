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
	FldCnt = xlsheet.cells(1,1).value &&field's count
	StartRow = xlsheet.cells(1,2).value-1&&Start Row
	xlSheet.rows("1:1").delete()
else
    xlBook = xlApp.Workbooks.Add(h+"\"+filename)
	xlSheet = xlBook.Worksheets(1)
	xlSheet.rows("1:1").insert()
	FldCnt = iif(Empty(aFldLst),fCount()-1,aLen(aFldLst,1))
	StartRow = 5
endif

if type("FldCnt")<>"N"
    return
endif

*set the border's style and align mode
RangeString = xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
RangeString ="$A$2:"+ RangeString
xlSheet.range(RangeString).borders(1).LineStyle = 7
xlSheet.range(RangeString).borders(2).LineStyle = 7
xlSheet.range(RangeString).borders(3).LineStyle = 7
xlSheet.range(RangeString).borders(4).LineStyle = 7
*xlSheet.range(RangeString).HorizontalAlignment = 3
*!*	xlSheet.range(RangeString).VerticalAlignment = 2
xlSheet.range(RangeString).Rowheight=14.25
xlSheet.range(RangeString).NumberFormatLocal = "@"
*xlSheet.columns(1).NumberFormatLocal = "@"
xlSheet.range(RangeString).font.name = "宋体"
xlSheet.range(RangeString).font.size = 9

RangeString = "$A$2:"+xlsheet.cells(2,FldCnt).address
xlSheet.range(RangeString).borders(3).LineStyle = 7&&12
RangeString = xlsheet.cells(RecCnt+StartRow-1,1).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
xlSheet.range(RangeString).borders(4).LineStyle = 7&&12
RangeString = "$A$2:"+xlsheet.cells(RecCnt+StartRow-1,1).address
xlSheet.range(RangeString).borders(1).LineStyle = 7&&12
RangeString = xlsheet.cells(2,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
xlSheet.range(RangeString).borders(2).LineStyle = 7&&12

*Setting the title
RangeString = xlsheet.cells(1,FldCnt).address
Rangestring ="$A$1:"+Rangestring
xlSheet.range(RangeString).select
xlSheet.range(RangeString).clear
xlSheet.range(RangeString).merge
*!*	xlSheet.cells(1,1).HorizontalAlignment = 0&&3
*!*	xlSheet.cells(1,1).VerticalAlignment = 2
xlSheet.cells(1,1).value = m.cTitle
xlSheet.cells(1,1).font.size = 18
xlSheet.cells(1,1).font.name = "黑体"
xlSheet.PageSetup.PrintTitleRows="$1:$"+ALLTRIM(STR(StartRow-1))
xlSheet.cells(1,1).HorizontalAlignment = 3

RangeString = xlsheet.cells(2,FldCnt).address
Rangestring ="$A$2:"+Rangestring
xlSheet.range(RangeString).font.name="黑体"

*!*	IF !empty(aFldLst)
*!*		FOR i=1 to FldCnt
*!*	        xlSheet.cells(2,i).value = alltrim(aFldLst[i,1])
*!*		ENDFOR
*!*	ENDIF

SELECT (aTable)
k=0
SCAN
    xlSheet.cells(StartRow+k,1).value = k+1
	FOR i=1 to FldCnt-1
	    mm=alltrim(FIELD(i))
	    IF !EMPTY(mm)
		    IF TYPE(mm)="N"
		        IF &mm =0
		            LOOP
		        ENDIF
		    ENDIF

            xlSheet.cells(StartRow+k,i+1).value = &mm
        ENDIF
	ENDFOR
    k=k+1
ENDSCAN
xlSheet.cells(StartRow+k-1,1).value = ""
FOR i=1 to FldCnt
    xlSheet.cells(StartRow+k-1,i+1).font.name = "黑体"
    xlSheet.cells(StartRow+k-1,i+1).font.bold = .t.
ENDFOR
	
xlSheet.name=ALLTRIM(dwmc(m.pdwbm))
xlApp.visible = .t.
RELEASE xlApp