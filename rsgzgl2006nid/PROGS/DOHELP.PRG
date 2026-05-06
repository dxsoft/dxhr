PARAMETERS tcTopic

nn=PCOUNT()

IF FILE(H+"\gzhlp.chm")
    SET HELP TO H+"\gzhlp.chm"
    IF nn=0
        HELP
    ELSE
        HELP &tcTopic
    ENDIF
ENDIF
