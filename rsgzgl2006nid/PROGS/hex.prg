FUNCTION Hex(lByte)     
x=''  
DO WHILE lByte>0     
IF lByte>=16     
y=lByte%16
ELSE     
y=lByte     
ENDIF     
IF y<10 .and. y>=0     
x=STR(y,1)+x     
ELSE     
x=CHR(65+y-10)+x     
ENDIF     
lByte=(lByte-y)/16     
ENDDO     
RETURN x     
ENDFUNC