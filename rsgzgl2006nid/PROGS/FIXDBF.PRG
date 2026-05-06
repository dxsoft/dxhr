PROCEDURE fixdbf

CPATH=GETDIR()
*!*	SET DEFAULT TO (cpath)
i=ADIR(apath,CPATH+"*.dbf")
FOR j=1 TO i
    tcfname=CPATH+apath[j,1]
	fhandle=FOPEN(tcfname,12)
	a=FREAD(fhandle,32)
	jllen=ASC(sUBSTR(a,12,1))*16^2+ASC(SUBSTR(a,11,1))
	jls=ASC(sUBSTR(a,8,1))*16^6+ASC(sUBSTR(a,7,1))*16^4+ASC(sUBSTR(a,6,1))*16^2+ASC(SUBSTR(a,5,1))
	first=ASC(sUBSTR(a,10,1))*16^2+ASC(sUBSTR(a,9,1))
	zds=(first-32-264)/32
	filesize=FSEEK(fhandle,0,2)
	IF filesize<>first+jllen*jls
		IF ((first-32-264)/32)=INT((first-32-264)/32)
		    FCHSIZE(fhandle,first+jllen*jls)
		ENDIF
	ENDIF

	FCLOSE(fhandle)
ENDFOR
