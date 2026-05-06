PARAMETERS tcZwbm

IF EMPTY(tcZwbm)
    RETURN SPACE(4)
ENDIF

IF LEFT(tcZwbm,2)="01"
	DO CASE
	CASE tcZwbm="0117" OR tcZwbm="0217" OR tcZwbm="0317"
	    tcZwbm="01B0"
	CASE tcZwbm="0115" OR tcZwbm="0215" OR tcZwbm="0315"
	    tcZwbm="01A0"
	CASE tcZwbm="0116" OR tcZwbm="0216" OR tcZwbm="0316"
	    tcZwbm="01A1"
	CASE tcZwbm="0113" OR tcZwbm="0213" OR tcZwbm="0313"
	    tcZwbm="0190"
	CASE tcZwbm="0114" OR tcZwbm="0214" OR tcZwbm="0314"
	    tcZwbm="0191"
	CASE tcZwbm="0111" OR tcZwbm="0211" OR tcZwbm="0311"
	    tcZwbm="0180"
	CASE tcZwbm="0112" OR tcZwbm="0212" OR tcZwbm="0312"
	    tcZwbm="0181"
	CASE tcZwbm="0118" OR tcZwbm="0218" OR tcZwbm="0318"
	    tcZwbm="01C0"
	CASE tcZwbm="0101" OR tcZwbm="0201" OR tcZwbm="0301"
	    tcZwbm="0110"
	CASE tcZwbm="0102" OR tcZwbm="0202" OR tcZwbm="0302"
	    tcZwbm="0120"
	CASE tcZwbm="0103" OR tcZwbm="0203" OR tcZwbm="0303"
	    tcZwbm="0130"
	CASE tcZwbm="0104" OR tcZwbm="0204" OR tcZwbm="0304"
	    tcZwbm="0140"
	CASE tcZwbm="0105" OR tcZwbm="0205" OR tcZwbm="0305"
	    tcZwbm="0150"
	CASE tcZwbm="0106" OR tcZwbm="0206" OR tcZwbm="0306"
	    tcZwbm="0151"
	CASE tcZwbm="0107" OR tcZwbm="0207" OR tcZwbm="0307"
	    tcZwbm="0160"
	CASE tcZwbm="0108" OR tcZwbm="0208" OR tcZwbm="0308"
	    tcZwbm="0161"
	CASE tcZwbm="0109" OR tcZwbm="0209" OR tcZwbm="0309"
	    tcZwbm="0170"
	CASE tcZwbm="0110" OR tcZwbm="0210" OR tcZwbm="0310"
	    tcZwbm="0171"
	CASE tcZwbm="0199" OR tcZwbm="0299" OR tcZwbm="0399"
	    tcZwbm="01FF"
	ENDCASE
ENDIF

IF LEFT(tcZwbm,2)="07"
    DO CASE
	CASE tcZwbm="0714"
	    tcZwbm="07B0"
	CASE tcZwbm="0716"
	    tcZwbm="07C0"
	CASE tcZwbm="0712"
	    tcZwbm="0790"
	CASE tcZwbm="0713"
	    tcZwbm="07A0"
	CASE tcZwbm="0706"
	    tcZwbm="0730"
	CASE tcZwbm="0707"
	    tcZwbm="0740"
	CASE tcZwbm="0708"
	    tcZwbm="0750"
	CASE tcZwbm="0709"
	    tcZwbm="0760"
	CASE tcZwbm="0710"
	    tcZwbm="0770"
	CASE tcZwbm="0711"
	    tcZwbm="0780"
	CASE tcZwbm="0799"
	    tcZwbm="07FF"
	ENDCASE
ELSE
    IF SUBSTR(tcZwbm,3,2)="99"
	    tcZwbm=LEFT(tcZwbm,2)+"FF"
	ENDIF
ENDIF

RETURN tcZwbm
