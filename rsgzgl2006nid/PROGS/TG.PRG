m.rzjl = rzjl(dwbm+grbm,dwsx)
xl = xl(dwbm+grbm,"2006.07")
IF !EMPTY(m.rzjl)
    m.zwbm=LEFT(m.rzjl,4)
    IF SUBSTR(m.rzjl,AT(",",m.rzjl,3)+1,AT(",",m.rzjl,4)-AT(",",m.rzjl,3)-1)="1"&&部队职务
        m.zwbm=dwsx+SUBSTR(m.zwbm,3,2)
        m.xrzwbm=xrzwbm(dwbm+grbm)
        IF !INLIST(SUBSTR(m.zwbm,3,1),"1","2","3","4","B","C") AND dwsx<>"07" AND m.zwbm<LEFT(ALLTRIM(m.xrzwbm)+"FFFF",4)&&部队职务高于现任职务，按虚职
            m.zwbm=LEFT(m.zwbm,3)+"1"
        ENDIF
    ENDIF
    rzsj = SUBSTR(m.rzjl,AT(",",m.rzjl,2)+1,AT(",",m.rzjl,3)-AT(",",m.rzjl,2)-1)
    zwkjnx = VAL(SUBSTR(m.rzjl,AT(",",m.rzjl,4)+1,AT(",",m.rzjl,5)-AT(",",m.rzjl,4)-1))
    rznx = 2006-VAL(LEFT(m.rzsj,4))+1-m.zwkjnx

    m.zwbm1=SUBSTR(m.rzjl,AT(",",m.rzjl,5)+1,AT(",",m.rzjl,6)-AT(",",m.rzjl,5)-1)
    IF SUBSTR(m.rzjl,AT(",",m.rzjl,8)+1,AT(",",m.rzjl,9)-AT(",",m.rzjl,8)-1)="1"&&部队职务
        m.zwbm1=dwsx+SUBSTR(m.zwbm1,3,2)
        IF !INLIST(SUBSTR(m.zwbm1,3,1),"1","2","3","4","B","C") AND dwsx<>"07"
            m.zwbm1=LEFT(m.zwbm1,3)+"1"
        ENDIF
    ENDIF
    IF !EMPTY(m.zwbm1)
        rzsj1 = SUBSTR(m.rzjl,AT(",",m.rzjl,7)+1,AT(",",m.rzjl,8)-AT(",",m.rzjl,7)-1)
        zwkjnx1 = VAL(SUBSTR(m.rzjl,AT(",",m.rzjl,9)+1,AT(",",m.rzjl,10)-AT(",",m.rzjl,9)-1))
        rznx1 = IIF(BETWEEN(VAL(LEFT(m.rzsj1,4)),1940,2006),2006-VAL(LEFT(m.rzsj1,4))+1-m.zwkjnx1,0)
    ELSE
        rxnz1=0    
    ENDIF
ENDIF

m.tgjbdc=thisform.prg1.tg06(m.zwbm,m.rznx,m.zwbm1,m.rznx1,m.tgnx,LEFT(m.xl,2))