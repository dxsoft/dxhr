PARAMETERS cText

cText=LEFT(cText+SPACE(60),60)

nn=ASC(SUBSTR(cText,19,1))
nn1=ASC(SUBSTR(cText,30,1))
cText=LEFT(cText,18)+SUBSTR(cText,20,10)+SUBSTR(cText,31,30)
FOR nIx=1 TO 58
    ctext=STUFF(ctext,nIx,1,CHR(BITXOR(ASC(SUBSTR(ctext,nIx,1)),nn1)))
    ctext=STUFF(ctext,nIx,1,CHR(BITXOR(ASC(SUBSTR(ctext,nIx,1)),nn)))
ENDFOR

PRIVATE nIx, nAsc1, nAsc2, cChar
FOR nIx=1 TO 29
   nAsc1=255-asc(subs(cText,nIx,1))
   nAsc2=255-asc(subs(cText,nIx+29,1))
   cChar=chr(int(nAsc1/16)*16+int(nAsc2/16))
   cText=stuff(cText,nIx,1,cChar)
   cChar=chr(mod(nAsc1,16)*16+mod(nAsc2,16))
   cText=stuff(cText,nIx+29,1,cChar)
ENDFOR

RETURN cText
