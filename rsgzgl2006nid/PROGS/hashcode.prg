FUNCTION hashcode

PARAMETERS tp

neg=.f.
src=0
IF src=0 AND LEN(tp)>0
    FOR i=1 TO LEN(tp)
        IF i=25
            aaa=1
        ENDIF
        src=31*src
        h=src
        
        aa=ASC(SUBSTR(tp,i,1))
        &&判断是否超整数范围
        IF src>2147483647
	        bin=''
	        DO WHILE .t.
	            bin=STR(MOD(h,2),1)+bin
	            h=int(h/2)
	            IF h=1
	                EXIT
	            ENDIF
	        ENDDO

	        bin="1"+bin
	        
	        IF LEN(bin)>=32
	            bin=RIGHT(bin,32)&&舍弃高位得补码
	            IF LEFT(bin,1)="1"&&负数，减1得反码，符号位不变，按位取反得原码
	                n=0
	                FOR j=1 TO 31
	                    IF LEFT(RIGHT(bin,j),1)='1'
	                        n=n+2^(j-1)
	                    ENDIF
	                ENDFOR
	                n=n-1&& 得反码

	                bin=''
			        DO WHILE .t.&&转二进制
			            bin=STR(MOD(n,2),1)+bin
			            n=int(n/2)
			            IF n=1
			                EXIT
			            ENDIF
			        ENDDO 
			        bin='1'+bin
			        bin=PADL(bin,31,'0')
			        
			                        
			        &&转十进制
			        bin1=''
	                FOR j=1 TO 31
	                    IF SUBSTR(bin,j,1)='1'
	                        bin1=bin1+"0"
	                    ELSE
	                        bin1=bin1+'1'
	                    ENDIF
	                ENDFOR
	                
	                
			        &&转十进制
			        n=INT(0)
	                FOR j=1 TO 31
	                    IF LEFT(RIGHT(bin1,j),1)='1'
	                        n=n+2^(j-1)
	                    ENDIF
	                ENDFOR
	                IF neg
    	                n=n+ASC(SUBSTR(tp,i,1))
    	                neg=.f.
    	            ELSE
    	                n=n-ASC(SUBSTR(tp,i,1))
    	                neg=.t.
                    ENDIF

	            ELSE
			        &&转十进制
			        n=0
	                FOR j=1 TO 31
	                    IF LEFT(RIGHT(bin,j),1)='1'
	                        n=n+2^(j-1)
	                    ENDIF
	                ENDFOR
	                IF neg
    	                n=n-ASC(SUBSTR(tp,i,1))
    	                neg=.t.
    	            ELSE
    	                n=n+ASC(SUBSTR(tp,i,1))
    	                neg=.f.
    	            ENDIF
	            ENDIF
	            src=n
	        ENDIF
	    ELSE
            IF neg
                src=src-ASC(SUBSTR(tp,i,1))
                neg=.t.
            ELSE
                src=src+ASC(SUBSTR(tp,i,1))
                neg=.f.
            ENDIF
        ENDIF        
    ENDFOR
    RETURN ABS(INT(n))
ENDIF
