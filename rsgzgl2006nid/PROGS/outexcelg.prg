LPARAMETERS aTable,cTitle,cTemplate,aFldLst,j

PRIVATE filename

&&jjjy不统计

*!*	SELECT xm,sfzh,IIF(zgxl="硕士学位研究生毕业" OR zgxl="研究生(硕士)",'研究生  ',IIF(zgxl="大学本科毕业" OR zgxl="本科","大学本科",IIF(zgxl="大专毕业" OR zgxl="专科","大学专科",IIF(zgxl="中专毕业","中专及以下",zgxl)))) as xl,b.dwmc,;
*!*	zwcc(zjbm),zjcc(zwbm2) as zjcc,gzdy(zwbm2) as gzdy,VAL(m.pnd)-VAL(LEFT(cjgzny,4)) as gznx,'否' as xz,'' as xzlb,'是' as zz,'' as bzlb,12 as ys,jbmc(jbgzjb2) as jb,zwgzdc2 as dc,(zwgzse2+jxgz+jbgzse2)*12 as jbengz,(zwgzse2+jxgz)*12 as zwgz,jbgzse2*12 as jbgz,;
*!*	gwjt2*12+0+xzgzbt+0+0+blfb2*12+jjjy2*12,gwjt2*12,000 as jkbyjt,xzgzbt,0 as ghb,0 as xzjt,(blfb2+jjjy2)*12 as ggbl,dfbt2*12,cb+txbt+wybt as ggx,nzj,00 as lg,wmj+mbkhj+pajsj+nxj+pskhj+lgbjt+wmcsj+qt as jlx FROM dryjbxx a LEFT JOIN dwbm b ON a.dwbm=b.dwbm INTO CURSOR lsk



*!*	SELECT xm,sfzh,IIF(zgxl="硕士学位研究生毕业" OR zgxl="研究生(硕士)",'研究生  ',IIF(zgxl="大学本科毕业" OR zgxl="本科","大学本科",IIF(zgxl="大专毕业" OR zgxl="专科","大学专科",IIF(zgxl="中专毕业","中专及以下",zgxl)))) as xl,b.dwmc,;
*!*	zwcc(zjbm),zjcc(zwbm2) as zjcc,gzdy(zwbm2) as gzdy,VAL(m.pnd)-VAL(LEFT(cjgzny,4)) as gznx,'否' as xz,'' as xzlb,'是' as zz,'' as bzlb,12 as ys,jbmc(jbgzjb2) as jb,zwgzdc2 as dc,(zwgzse2+jxgz+jbgzse2) as jbengz,(zwgzse2+jxgz) as zwgz,jbgzse2 as jbgz,;
*!*	gwjt2+0+xzgzbt+0+0+blfb2,gwjt2,000 as jkbyjt,xzgzbt,0 as ghb,0 as xzjt,blfb2 as ggbl,dfbt2,cb+txbt+wybt as ggx,nzj,00 as lg,wmj+mbkhj+pajsj+nxj+pskhj+lgbjt+wmcsj+qt as jlx FROM dryjbxx a LEFT JOIN dwbm b ON a.dwbm=b.dwbm INTO CURSOR lsk



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
	xlSheet = xlBook.Worksheets(2)

	FldCnt =46
	StartRow = 3

	SELECT (aTable)
	k=0
	RecCnt=0
	SCAN FOR LEFT(zwbm2,2)<>'03'
		FOR i=1 to FldCnt
		    mm=alltrim(FIELD(i))
		    IF !EMPTY(mm)
			    IF TYPE(mm)="N"
			        IF &mm =0
			            LOOP
			        ELSE
        	            xlSheet.cells(StartRow+k,i).value = &mm
			        ENDIF
			    ELSE
    	            xlSheet.cells(StartRow+k,i).value = ALLTRIM(&mm)
			    ENDIF
	        ENDIF
		ENDFOR

	    k=k+1
	    RecCnt=RecCnt+1
	ENDSCAN


	&&员额
	xlSheet = xlBook.Worksheets(3)

	FldCnt =41
	StartRow = 3

	SELECT (aTable)
	k=0
	RecCnt=0
	j=1
	SCAN FOR LEFT(zwbm2,2)='03'
	    j=0
		FOR i=1 to FldCnt
		    mm=alltrim(FIELD(i))
		    IF !EMPTY(mm) AND UPPER(mm)<>'ZJCC' AND UPPER(mm)<>'GZDY' AND UPPER(mm)<>'ZWGZ' AND UPPER(mm)<>'JBGZ'
        	    j=j+1
			    IF TYPE(mm)="N"
			        IF &mm =0
			            LOOP
			        ELSE
        	            xlSheet.cells(StartRow+k,j).value = &mm
			        ENDIF
			    ELSE
    	            xlSheet.cells(StartRow+k,j).value = ALLTRIM(&mm)
			    ENDIF
	        ENDIF
		ENDFOR

	    k=k+1
	    RecCnt=RecCnt+1
	ENDSCAN





*!*		*set the border's style and align mode
*!*		RangeString = xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*		RangeString ="$A$4:"+ RangeString
*!*		xlSheet.range(RangeString).borders(1).LineStyle = 7
*!*		xlSheet.range(RangeString).borders(2).LineStyle = 7
*!*		xlSheet.range(RangeString).borders(3).LineStyle = 7
*!*		xlSheet.range(RangeString).borders(4).LineStyle = 7
*!*		xlSheet.range("$A$"+ALLTRIM(STR(StartRow))+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address).Rowheight=14.25
*!*		xlSheet.range(RangeString).font.name = "宋体"
*!*		xlSheet.range(RangeString).font.size = 9

*!*		RangeString = "$A$4:"+xlsheet.cells(4,FldCnt).address
*!*		xlSheet.range(RangeString).borders(3).LineStyle = 7&&12
*!*		RangeString = xlsheet.cells(RecCnt+StartRow-1,1).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*		xlSheet.range(RangeString).borders(4).LineStyle = 7&&12
*!*		RangeString = "$A$4:"+xlsheet.cells(RecCnt+StartRow-1,1).address
*!*		xlSheet.range(RangeString).borders(1).LineStyle = 7&&12
*!*		RangeString = xlsheet.cells(4,FldCnt).address+":"+xlsheet.cells(RecCnt+StartRow-1,FldCnt).address
*!*		xlSheet.range(RangeString).borders(2).LineStyle = 7&&12

FINALLY
	xlApp.visible = .t.
	RELEASE xlApp
ENDTRY