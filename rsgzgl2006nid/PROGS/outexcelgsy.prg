LPARAMETERS aTable,cTitle,cTemplate,aFldLst,j

PRIVATE filename

&&jjjy不统计

**Create and set the excel's style
try
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

    xlBook = xlApp.Workbooks.Add(h+"\"+cTemplate)
	xlSheet = xlBook.Worksheets(1)

	FldCnt =58
	StartRow = 7

	SELECT (aTable)
	k=0
	RecCnt=0
	SCAN
        xlSheet.cells(StartRow+k,1).value = k+1
		FOR i=1 to FldCnt+2
		    mm=alltrim(FIELD(i))
		    IF !EMPTY(mm)
			    IF TYPE(mm)="N"
			        IF &mm =0
			            LOOP
			        ELSE
        	            xlSheet.cells(StartRow+k,i+1).value = &mm
			        ENDIF
			    ELSE
    	            xlSheet.cells(StartRow+k,i+1).value = ALLTRIM(&mm)
			    ENDIF
	        ENDIF
		ENDFOR

	    k=k+1
	    RecCnt=RecCnt+1
	ENDSCAN

    xlSheet.range("S7:S"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[7]+RC[8]+RC[9]+RC[17]+RC[20]+RC[24]+RC[28]+RC[34]+RC[35]"
    xlSheet.range("V7:V"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-2]+RC[-1]"
    xlSheet.range("Y7:Y"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-2]+RC[-1]"
    xlSheet.range("Z7:Z"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-4]+RC[-1]"
    xlSheet.range("AJ7:AJ"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=SUM(RC[-7]:RC[-1])"
    xlSheet.range("AM7:AM"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-2]+RC[-1]"
    xlSheet.range("AQ7:AQ"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("AU7:AU"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BA7:BA"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-5]+RC[-4]+RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BE7:BE"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-38]+RC[-2]+RC[-1]"


    &&样式中公式列固定，但行未固定，实际赋值会在当前所在行上+给定的值，如下边的  $Q7 ,所以在赋值前，必须先定位，下边一行必不可少，否则，设置的结果会不确定
    xlSheet.cells(7,1).select
    
    &&注意公式中的$的使用,只固定列，格式自动向下填充
    &&岗位工资校验
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=$T7<>VLOOKUP($M7,Sheet1!$CA$7:$CD$46,2,FALSE)*$R7')&&公式里需向下错3行不知何原因
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色


    &&薪级工资校验
    xlSheet.range("W7:W"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("W7:W"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=$W7<>VLOOKUP(LEFT($M7,2)&$Q7,Sheet1!$CG$7:$CJ$268,2,FALSE)*$R7')
    xlSheet.range("W7:W"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色

    &&调标增资额校验
    xlSheet.range("AA7:AA"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("AA7:AA"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($AA7<>VLOOKUP($BG7,Sheet1!$CA$7:$CD$46,3,FALSE)*6,$AA7<>VLOOKUP($BG7,Sheet1!$CA$7:$CD$46,4,FALSE)*6)')&&公式里需向下错3行不知何原因
    xlSheet.range("AA7:AA"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色


    &&薪级工资调标增资额校验
    xlSheet.range("AB7:AB"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("AB7:AB"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($AB7<>VLOOKUP(LEFT($BG7,2)&$BH7,Sheet1!$CG$7:$CJ$268,3,FALSE)*6,$AB7<>VLOOKUP(LEFT($BG7,2)&$BH7,Sheet1!$CG$7:$CJ$268,4,FALSE)*6)')
    xlSheet.range("AB7:AB"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色
    
    
    

	*set the border's style and align mode
	RangeString = xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
	RangeString ="$A$4:"+ RangeString
	xlSheet.range(RangeString).borders(1).LineStyle = 7
	xlSheet.range(RangeString).borders(2).LineStyle = 7
	xlSheet.range(RangeString).borders(3).LineStyle = 7
	xlSheet.range(RangeString).borders(4).LineStyle = 7
	xlSheet.range("$A$"+ALLTRIM(STR(StartRow))+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address).Rowheight=14.25
	xlSheet.range(RangeString).font.name = "宋体"
	xlSheet.range(RangeString).font.size = 9

	RangeString = "$A$4:"+xlsheet.cells(4,FldCnt).address
	xlSheet.range(RangeString).borders(3).LineStyle = 7&&12
	RangeString = xlsheet.cells(RecCnt+StartRow-1,1).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
	xlSheet.range(RangeString).borders(4).LineStyle = 7&&12
	RangeString = "$A$4:"+xlsheet.cells(RecCnt+StartRow-1,1).address
	xlSheet.range(RangeString).borders(1).LineStyle = 7&&12
	RangeString = xlsheet.cells(4,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
	xlSheet.range(RangeString).borders(2).LineStyle = 7&&12

FINALLY
	xlApp.visible = .t.
	RELEASE xlApp
ENDTRY