FUNCTION jbgzjs  &&级别工资晋升，由现职务及工作年限、原级别和倒级差，确定现级别

PARAMETERS tcZwbm,tcSrny,tnGznx,tcYjb,tcYdjc

**初始化，'9' 表明无变化
PRIVATE v_jb,v_djc,v_khnd

v_jb="99"
v_djc=" "
v_khnd="9999"

v_yjb=tcYjb
v_ydjc=tcYdjc
v_zgjb=zgjb(tcZwbm)

v_jb=gljb(tcZwbm,tnGznx)

IF AT("d",v_jb)>0
    IF RIGHT(v_jb,1)<v_ydjc
        v_djc=val(v_ydjc)-(val(v_yjb)-val(v_zgjb))
        v_djc=allt(str(v_djc))
    ELSE
        v_djc=right(v_jb,1)
    ENDIF
    
    v_jb=v_zgjb
ELSE

    IF val(v_jb)<val(v_yjb)
	    IF val(v_ydjc)>0   &&冲消倒级差计算
	        v_djc=val(v_ydjc)-(val(v_yjb)-val(v_jb))
	        IF v_djc< 0
	            v_djc=0
	        ENDIF
	        v_djc=allt(str(v_djc))
	    ELSE
	        v_djc=" "
	    ENDIF 
    ELSE
        v_jb=val(v_yjb)-val(v_ydjc)
        v_jb1=allt(str(v_jb))
        
        IF val(v_jb1)<val(v_zgjb)
            v_jb=v_zgjb
            v_djc=val(v_zgjb)-val(v_jb1)
	        v_djc=allt(str(v_djc))
        ELSE
            v_jb=v_jb1
            v_djc=" "
        ENDIF
    ENDIF
    
ENDIF

IF val(v_jb)-val(v_djc)<>val(v_yjb)-val(v_ydjc)&&级别发生了变化
    IF subs(tcSrny,6,2)='12'
        v_khnd=allt(str(val(left(tcSrny,4))+1))
    ELSE 
        v_khnd=left(tcSrny,4)
    ENDIF
ENDIF
   
RETURN v_khnd+v_djc+v_jb
