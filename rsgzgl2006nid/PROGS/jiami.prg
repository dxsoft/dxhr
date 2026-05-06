PARAMETERS cText

cText=LEFT(cText+"鼎星软件有限公司梁斌"+SPACE(50),58)

PRIVATE nIx, nAsc1, nAsc2, cChar
FOR nIx=1 TO 29
   nAsc1=255-asc(subs(cText,nIx,1))
   nAsc2=255-asc(subs(cText,nIx+29,1))
   cChar=chr(int(nAsc1/16)*16+int(nAsc2/16))
   cText=stuff(cText,nIx,1,cChar)
   cChar=chr(mod(nAsc1,16)*16+mod(nAsc2,16))
   cText=stuff(cText,nIx+29,1,cChar)
ENDFOR

nn=INT(MOD(RAND()*1000,256))
nn1=INT(MOD(RAND()*1000,256))
FOR nIx=1 TO 58
    ctext=STUFF(ctext,nIx,1,CHR(BITXOR(ASC(SUBSTR(ctext,nIx,1)),nn)))
    ctext=STUFF(ctext,nIx,1,CHR(BITXOR(ASC(SUBSTR(ctext,nIx,1)),nn1)))
ENDFOR
cText=LEFT(cText,18)+CHR(nn)+CHR(nn1)+SUBSTR(cText,19)

RETURN cText
