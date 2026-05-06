FUNCTION crack

PARAMETERS cText
PRIVATE nIx, nAsc1, nAsc2, cChar

cText=LEFT(cText+SPACE(50),50)
FOR nIx=1 TO 25
   nAsc1=255-asc(subs(cText,nIx,1))
   nAsc2=255-asc(subs(cText,nIx+25,1))
   cChar=chr(int(nAsc1/16)*16+int(nAsc2/16))
   cText=stuff(cText,nIx,1,cChar)
   cChar=chr(mod(nAsc1,16)*16+mod(nAsc2,16))
   cText=stuff(cText,nIx+25,1,cChar)
ENDFOR

RETURN cText
