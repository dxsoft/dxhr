FUNCTION WordToHex(lvalue)     

BITS_TO_A_BYTE=8

lResult=''     
FOR lCount = 0 TO 3     
lByte = BITAND(RShift(lvalue, lCount * BITS_TO_A_BYTE) , m_lOnBits(BITS_TO_A_BYTE ))     
lResult = lResult + RIGHT("0" + Hex(lByte), 2)     
ENDFOR     
RETURN lResult     

ENDFUNC

FUNCTION LShift(lvalue, iShiftBits)     
IF iShiftBits = 0      
RETURN lvalue     
ELSE     
IF iShiftBits = 31      
IF BITAND(lvalue , 1)<>0      
RETURN 0x80000000     
ELSE     
RETURN 0     
ENDIF     
ENDIF     
ENDIF     

IF BITAND(lvalue , m_l2Power(31 - iShiftBits))<>0      
RETURN BITOR( (BITAND(lvalue , m_lOnBits(31 - (iShiftBits + 1))) * m_l2Power(iShiftBits)) , 0x80000000)     
ELSE     
RETURN (BITAND(lvalue , m_lOnBits(31 - iShiftBits)) * m_l2Power(iShiftBits))     
ENDIF     
ENDFUNC     

FUNCTION RShift(lvalue, iShiftBits)     
IF iShiftBits = 0      
RETURN lvalue     
ELSE     
IF iShiftBits = 31      
IF BITAND(lvalue , 0x80000000)      
RETURN 1     
ELSE     
RETURN 0     
ENDIF     
ENDIF     
ENDIF
RShift2 = INT(BITAND(lvalue , 0x7FFFFFFE) / m_l2Power(iShiftBits))     
IF BITAND(lvalue , 0x80000000)<>0      
RShift2 =BITOR (RShift2 , INT(0x40000000 / m_l2Power(iShiftBits - 1)))     
ENDIF     

RETURN RShift2     
ENDFUNC     

FUNCTION RotateLeft(lvalue, iShiftBits)     
RETURN BITOR(LShift(lvalue, iShiftBits) ,RShift(lvalue, (32 - iShiftBits)))     
ENDFUNC 