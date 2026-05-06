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

	FldCnt =76
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

    xlSheet.range("P7:P"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[7]+RC[8]+RC[9]+RC[30]+RC[35]+RC[41]+RC[48]+RC[54]+RC[55]+RC[56]"
    xlSheet.range("S7:S"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-2]+RC[-1]"
    xlSheet.range("V7:V"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-2]+RC[-1]"
    xlSheet.range("W7:W"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-4]+RC[-1]"
    xlSheet.range("AT7:AT"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=SUM(RC[-20]:RC[-1])"
    xlSheet.range("AY7:AY"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-4]+RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BE7:BE"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-5]+RC[-4]+RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BL7:BL"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-4]+RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BR7:BR"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-5]+RC[-4]+RC[-3]+RC[-2]+RC[-1]"
    xlSheet.range("BW7:BW"+ALLTRIM(STR(StartRow+k-1))).FormulaR1C1="=RC[-59]+RC[-2]+RC[-1]"

*!*	    xlSheet.range("Q7:Q"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Type=

    &&注意公式中的$的使用,只固定列，格式自动向下填充
    
    
    &&样式中公式列固定，但行未固定，实际赋值会在当前所在行上+给定的值，如下边的  $Q7 ,所以在赋值前，必须先定位，下边一行必不可少，否则，设置的结果会不确定
    xlSheet.cells(7,1).select
    
    &&岗位工资校验
    xlSheet.range("Q7:Q"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("Q7:Q"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($Q7<>VLOOKUP($H7,Sheet1!$CA$7:$CE$46,2,FALSE)*$O7,$Q7<>VLOOKUP($H7,Sheet1!$CA$7:$CE$46,4,FALSE)*$O7)')&&公式里需向下错3行不知何原因
    xlSheet.range("Q7:Q"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色


    &&薪级工资校验
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($T7<>VLOOKUP(LEFT($H7,2)&$N7,Sheet1!$CG$7:$CK$281,2,FALSE)*$O7,$T7<>VLOOKUP(LEFT($H7,2)&$N7,Sheet1!$CG$7:$CK$281,4,FALSE)*$O7)')
    xlSheet.range("T7:T"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6        &&单元格底纹黄色

    &&调标增资额校验
    xlSheet.range("X7:X"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete
    xlSheet.range("X7:X"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($X7<>VLOOKUP($BY7,Sheet1!$CA$7:$CE$46,3,FALSE)*6,$X7<>VLOOKUP($BY7,Sheet1!$CA$7:$CE$46,5,FALSE)*6)')&&公式里需向下错3行不知何原因
    xlSheet.range("X7:X"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6       &&单元格底纹黄色


    &&薪级工资调标增资额校验
    xlSheet.range("Y7:Y"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Delete

*!*	    xlSheet.range("Y7:Y"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($Y7<>VLOOKUP(LEFT($H7,2)&($N7-1),$CG$7:$CK$281,3,FALSE)*6,$Y7<>VLOOKUP(LEFT($H7,2)&($N7-1),$CG$7:$CK$281,5,FALSE)*6)')

    xlSheet.range("Y7:Y"+ALLTRIM(STR(StartRow+k-1))).FormatConditions.Add(2,,'=AND($Y7<>VLOOKUP(LEFT($BY7,2)&$BZ7,Sheet1!$CG$7:$CL$281,3,FALSE)*6,$Y7<>VLOOKUP(LEFT($BY7,2)&$BZ7,Sheet1!$CG$7:$CL$281,5,FALSE)*6,$Y7<>VLOOKUP(LEFT($BY7,2)&$BZ7,Sheet1!$CG$7:$CL$281,6,FALSE)*6)')

    xlSheet.range("Y7:Y"+ALLTRIM(STR(StartRow+k-1))).FormatConditions(1).Interior.ColorIndex=6       &&单元格底纹黄色


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