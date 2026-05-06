PARAMETERS c1,c2,c3,c4

IF TYPE('myexcel')='O'
   myexcel.quit
ENDIF

v_gzq=SELECT(0)
crtvcwjl(.t.,conn)

IF c1<>2012
   INSERT INTO cwjl (时间,错误代码,错误信息,程序代码,程序名) VALUES (DATETIME(),c1,LEFT(c2,60),left(c3,254),c4)
ENDIF
TABLEUPDATE(1,.t.,"cwjl")
USE IN cwjl
SELECT (v_gzq)

IF c1=1707 or c1=1561 or c1=1429 or c1=1520 or c1=2012 or ('不能从' $ c2) or c1=1712 or c1=1961
   RETURN
ENDIF

*!*	IF c1=1102 or c1=1002
*!*	   IF 'reports' $ c3
*!*	       RETURN 
*!*	   ENDIF
*!*	   IF MESSAGEBOX('报表文件损坏!',5+32+0,'系统提示')=4
*!*	       RETRY
*!*	   ELSE
*!*	       CANCEL
*!*	   ENDIF 
*!*	ENDIF

*!*	IF c1=1951
*!*	   IF MESSAGEBOX('不能清除正在使用的对象, 请稍后重试!',5+32+0,'系统提示')=4
*!*	       RETRY
*!*	   ELSE
*!*	       CANCEL
*!*	   ENDIF 
*!*	ENDIF

IF c1=39
   RETURN
ENDIF

IF c1=1531
   MESSAGEBOX('试图移除索引文件中的字段失败.',1+64+256,'系统提示')
   CANCEL
ENDIF
 
*!*	x=MESSAGEBOX("错误代码: "+ALLTRIM(STR(c1))+CHR(13)+CHR(10)+CHR(13)+CHR(10)+"错误信息: "+c2,2+48+0,"程序运行错误")

x=MESSAGEBOX("错误代码: "+ALLTRIM(STR(c1))+CHR(13)+CHR(10)+CHR(13)+CHR(10)+"错误信息: "+c2,0+48+0,"程序运行错误")
DO CASE
CASE x=3
    CANCEL 
CASE x=4
    RETRY
CASE x=1
    RETURN          
OTHERWISE
    RETURN
ENDCASE
