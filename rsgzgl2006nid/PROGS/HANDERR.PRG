FUNCTION handerr
PARAMETERS tcLb
IF tcLb="ysb"
	IF ERROR()=1102
	    IF MESSAGEBOX("软盘损坏或软盘写保护或未插磁盘, 请插入空白软盘. 准备好后按[确定].",1+64,"消息")=1
	        RETURN 1
	    ELSE
		    RETURN 0
		ENDIF
	ENDIF
ENDIF

MESSAGEBOX("运行错误! 错误代码: "+ALLTRIM(STR(ERROR())),64,"错误")
