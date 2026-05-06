FUNCTION UncompressZip(cZipFile, cDestDir)
    LOCAL oShell, nZip, nDest
    cZipFile = FULLPATH(cZipFile)
    cDestDir = FULLPATH(cDestDir)
    
    * 检查 ZIP 文件是否存在
    IF !FILE(cZipFile)
        MESSAGEBOX("ZIP 文件不存在！", 16, "错误")
        RETURN .F.
    ENDIF
    
    * 创建目标目录
    IF !DIRECTORY(cDestDir)
        IF !MKDIR(cDestDir)
            MESSAGEBOX("无法创建目标目录！", 16, "错误")
            RETURN .F.
        ENDIF
    ENDIF
    
    oShell = CREATEOBJECT("Shell.Application")
    nZip = oShell.NameSpace(cZipFile)
    nDest = oShell.NameSpace(cDestDir)
    IF ISNULL(nZip) OR ISNULL(nDest)
        MESSAGEBOX("无法访问 ZIP 或目标目录！", 16, "错误")
        RETURN .F.
    ENDIF
    
    * 复制 ZIP 内容到目标目录（4=不显示进度框，16=自动覆盖）
    nDest.CopyHere(nZip.Items(), 4 + 16)
    
    * 等待操作完成（最多等待 30 秒）
    LOCAL lnWaitTime
    lnWaitTime = 0
    DO WHILE lnWaitTime < 30
        IF nDest.Items().Count >= nZip.Items().Count
            EXIT
        ENDIF
        INKEY(1)
        lnWaitTime = lnWaitTime + 1
    ENDDO
    
    RETURN .T.
ENDFUNC