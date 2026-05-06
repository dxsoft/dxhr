PROCEDURE initi

IF MESSAGEBOX("系统初始化将删除所有的人员信息(单位信息除外), 请慎重选择! "+CHR(10)+CHR(13)+CHR(10)+CHR(13)+"是否继续?",4+48+256,"警告!")=6
    WAIT "正在进行系统初始化, 请耐心等待......" WINDOW AT SROWS()/2,SCOLS()/2-10 NOWAIT
    
    IF UPPER(m.dbtype)="MYSQL"
	    aa=SQLEXEC(conn, "call usp_init(@result)")
	    AERROR(bbb)
    ELSE
	    result=0
	    aa=SQLEXEC(conn, "p_init ?@result")
	    IF result>0
	        MESSAGEBOX("初始化失败！",64,"提示")
		ELSE
		    MESSAGEBOX("初始化完毕!",64,"消息")
	    ENDIF
	ENDIF
ENDIF
        