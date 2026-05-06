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

FUNCTION AddUnsigned(lX, lY)    

lX8 = BITAND(lX , 0x80000000)    
lY8 = BITAND(lY , 0x80000000)    
lX4 = BITAND(lX , 0x40000000)    
lY4 = BITAND(lY , 0x40000000)    

lResult = BITAND(lX , 0x3FFFFFFF) + BITAND(lY , 0x3FFFFFFF)    

IF BITAND(lX4 , lY4)<> 0     
lResult = BITXOR(BITXOR(BITXOR(lResult , 0x80000000) , lX8) , lY8)    
ELSE    
IF BITOR(lX4 , lY4)<> 0     
IF BITAND(lResult , 0x40000000)<> 0     
lResult = BITXOR(BITXOR(BITXOR(lResult , 0xC0000000) , lX8) , lY8)    
ELSE    
lResult = BITXOR(BITXOR(BITXOR(lResult , 0x40000000) , lX8) , lY8)   


ENDIF    
ELSE    
lResult = BITXOR(BITXOR( lResult , lX8) , lY8)    
ENDIF    
ENDIF    
RETURN lResult    
ENDFUNC    

FUNCTION md5_F(x, y, z)    
RETURN BITOR(BITAND(x , y) , BITAND(BITNOT(x) , z))    
ENDFUNC    

FUNCTION md5_G(x, y, z)    
RETURN BITOR(BITAND(x , z) , BITAND(y , BITNOT(z)))    
ENDFUNC    

FUNCTION md5_H(x, y, z)    
RETURN BITXOR(BITXOR(x , y) , z)    
ENDFUNC    

FUNCTION md5_I(x, y, z)    
RETURN BITXOR(y , BITOR(x , BITNOT(z)))    
ENDFUNC    

PROCEDURE md5_FF(a, b, c, d, x, s, ac)    
a = AddUnsigned(a, AddUnsigned(AddUnsigned(md5_F(b, c, d), x), ac))    
a = RotateLeft(a, s)    
a = AddUnsigned(a, b)    
ENDPROC    

PROCEDURE md5_GG(a, b, c, d, x, s, ac)    
a = AddUnsigned(a, AddUnsigned(AddUnsigned(md5_G(b, c, d), x), ac))    
a = RotateLeft(a, s)    
a = AddUnsigned(a, b)    
ENDPROC    

PROCEDURE md5_HH(a, b, c, d, x, s, ac)    
a = AddUnsigned(a, AddUnsigned(AddUnsigned(md5_H(b, c, d), x), ac))    
a = RotateLeft(a, s)    
a = AddUnsigned(a, b)    
ENDPROC    

PROCEDURE md5_II(a, b, c, d, x, s, ac)    
a = AddUnsigned(a, AddUnsigned(AddUnsigned(md5_I(b, c, d), x), ac))    
a = RotateLeft(a, s)    
a = AddUnsigned(a, b)    
ENDPROC    

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

FUNCTION WordToHex(lvalue)    
BITS_TO_A_BYTE = 8
BYTES_TO_A_WORD = 4    
BITS_TO_A_WORD = 32    
DIME m_lOnBits(31)    
DIME m_l2Power(31)    
m_lOnBits(1) = 1    
m_lOnBits(2) = 3    
m_lOnBits(3) = 7    
m_lOnBits(4) = 15    
m_lOnBits(5) = 31    
m_lOnBits(6) = 63    
m_lOnBits(7) = 127    
m_lOnBits(8) = 255    
m_lOnBits(9) = 511    
m_lOnBits(10) = 1023    
m_lOnBits(11) = 2047    
m_lOnBits(12) = 4095    
m_lOnBits(13) = 8191    
m_lOnBits(14) = 16383    
m_lOnBits(15) = 32767    
m_lOnBits(16) = 65535    
m_lOnBits(17) = 131071    
m_lOnBits(18) = 262143    
m_lOnBits(19) = 524287    
m_lOnBits(20) = 1048575    
m_lOnBits(21) = 2097151    
m_lOnBits(22) = 4194303    
m_lOnBits(23) = 8388607    
m_lOnBits(24) = 16777215    
m_lOnBits(25) = 33554431    
m_lOnBits(26) = 67108863    
m_lOnBits(27) = 134217727    
m_lOnBits(28) = 268435455    
m_lOnBits(29) = 536870911    
m_lOnBits(30) = 1073741823    
m_lOnBits(31) = 2147483647    

m_l2Power(1) = 1    
m_l2Power(2) = 2    
m_l2Power(3) = 4    
m_l2Power(4) = 8    
m_l2Power(5) = 16    
m_l2Power(6) = 32    
m_l2Power(7) = 64    
m_l2Power(8) = 128    
m_l2Power(9) = 256    
m_l2Power(10) = 512    
m_l2Power(11) = 1024    
m_l2Power(12) = 2048    
m_l2Power(13) = 4096    
m_l2Power(14) = 8192    
m_l2Power(15) = 16384    
m_l2Power(16) = 32768    
m_l2Power(17) = 65536    
m_l2Power(18) = 131072    
m_l2Power(19) = 262144    
m_l2Power(20) = 524288    
m_l2Power(21) = 1048576    
m_l2Power(22) = 2097152    
m_l2Power(23) = 4194304    
m_l2Power(24) = 8388608    
m_l2Power(25) = 16777216    
m_l2Power(26) = 33554432    
m_l2Power(27) = 67108864    
m_l2Power(28) = 134217728    
m_l2Power(29) = 268435456    
m_l2Power(30) = 536870912    
m_l2Power(31) = 1073741824    



S11 = 7    
S12 = 12    
S13 = 17    
S14 = 22    
S21 = 5    
S22 = 9    
S23 = 14    
S24 = 20    
S31 = 4    
S32 = 11    
S33 = 16    
S34 = 23    
S41 = 6    
S42 = 10    
S43 = 15    
S44 = 21    

*********Function ConvertToWordArray(sMessage)    

MODULUS_BITS = 512    
CONGRUENT_BITS = 448    

lResult="" 
FOR lCount = 0 TO 3    
lByte = BITAND(RShift(lvalue, lCount * BITS_TO_A_BYTE) , m_lOnBits(BITS_TO_A_BYTE ))    
lResult = lResult +RIGHT("0" + Hex(lByte), 2)    
ENDFOR    
RETURN lResult    

ENDFUNC

FUNCTION MD5(sMessage)    

BITS_TO_A_BYTE = 8
BYTES_TO_A_WORD = 4    
BITS_TO_A_WORD = 32    
DIME m_lOnBits(31)    
DIME m_l2Power(31)    
m_lOnBits(1) = 1    
m_lOnBits(2) = 3    
m_lOnBits(3) = 7    
m_lOnBits(4) = 15    
m_lOnBits(5) = 31    
m_lOnBits(6) = 63    
m_lOnBits(7) = 127    
m_lOnBits(8) = 255    
m_lOnBits(9) = 511    
m_lOnBits(10) = 1023    
m_lOnBits(11) = 2047    
m_lOnBits(12) = 4095    
m_lOnBits(13) = 8191    
m_lOnBits(14) = 16383    
m_lOnBits(15) = 32767    
m_lOnBits(16) = 65535    
m_lOnBits(17) = 131071    
m_lOnBits(18) = 262143    
m_lOnBits(19) = 524287    
m_lOnBits(20) = 1048575    
m_lOnBits(21) = 2097151    
m_lOnBits(22) = 4194303    
m_lOnBits(23) = 8388607    
m_lOnBits(24) = 16777215    
m_lOnBits(25) = 33554431    
m_lOnBits(26) = 67108863    
m_lOnBits(27) = 134217727    
m_lOnBits(28) = 268435455    
m_lOnBits(29) = 536870911    
m_lOnBits(30) = 1073741823    
m_lOnBits(31) = 2147483647    

m_l2Power(1) = 1    
m_l2Power(2) = 2    
m_l2Power(3) = 4    
m_l2Power(4) = 8    
m_l2Power(5) = 16    
m_l2Power(6) = 32    
m_l2Power(7) = 64    
m_l2Power(8) = 128    
m_l2Power(9) = 256    
m_l2Power(10) = 512    
m_l2Power(11) = 1024    
m_l2Power(12) = 2048    
m_l2Power(13) = 4096    
m_l2Power(14) = 8192    
m_l2Power(15) = 16384    
m_l2Power(16) = 32768    
m_l2Power(17) = 65536    
m_l2Power(18) = 131072    
m_l2Power(19) = 262144    
m_l2Power(20) = 524288    
m_l2Power(21) = 1048576    
m_l2Power(22) = 2097152    
m_l2Power(23) = 4194304    
m_l2Power(24) = 8388608    
m_l2Power(25) = 16777216    
m_l2Power(26) = 33554432    
m_l2Power(27) = 67108864    
m_l2Power(28) = 134217728    
m_l2Power(29) = 268435456    
m_l2Power(30) = 536870912    
m_l2Power(31) = 1073741824    



S11 = 7    
S12 = 12    
S13 = 17    
S14 = 22    
S21 = 5    
S22 = 9    
S23 = 14    
S24 = 20    
S31 = 4    
S32 = 11    
S33 = 16    
S34 = 23    
S41 = 6    
S42 = 10    
S43 = 15    
S44 = 21    

*********Function ConvertToWordArray(sMessage)    

MODULUS_BITS = 512    
CONGRUENT_BITS = 448    


lMessageLength = LEN(sMessage)    

lNumberOfWords = (((lMessageLength + INT(INT((MODULUS_BITS - CONGRUENT_BITS) / BITS_TO_A_BYTE)) /INT (MODULUS_BITS / BITS_TO_A_BYTE)) )+ 1) * INT(MODULUS_BITS /BITS_TO_A_WORD)    

DIME lWordArray(lNumberOfWords )    
STORE 0 TO lWordArray    

lBytePosition = 0    
lByteCount = 0    
DO WHILE lByteCount < lMessageLength    
lWordCount = INT(lByteCount / BYTES_TO_A_WORD)    
lBytePosition = MOD(lByteCount , BYTES_TO_A_WORD) * BITS_TO_A_BYTE    
lWordArray(lWordCount+1) =BITOR( lWordArray(lWordCount+1) , LShift(ASC(SUBSTR(sMessage, lByteCount + 1, 1)), lBytePosition))    
lByteCount = lByteCount + 1    
ENDDO    

lWordCount = INT(lByteCount / BYTES_TO_A_WORD)    
lBytePosition = MOD(lByteCount , BYTES_TO_A_WORD) * BITS_TO_A_BYTE    

lWordArray(lWordCount + 1) =BITOR( lWordArray(lWordCount + 1) , LShift(0x80, lBytePosition))    

lWordArray(lNumberOfWords - 1) = LShift(lMessageLength, 3)    
lWordArray(lNumberOfWords ) = RShift(lMessageLength, 29)    

***************************    

a = 0x67452301    
b = 0xEFCDAB89    
c = 0x98BADCFE    
d = 0x10325476    

DIME x(lNumberOfWords)    

FOR k = 1 TO lNumberOfWords    
x(k)=lWordArray(k)    
ENDFOR    

*****设置错误处理程序，因Visual FoxPro程序本身所限，对大数的处理能力不够    
ON ERROR do err_treat    

FOR k = 1 TO lNumberOfWords STEP 16    
AA = a    
BB = b    
CC = c    
DD = d    

DO md5_FF WITH a, b, c, d, x(k + 0), S11, 0xD76AA478    
DO md5_FF WITH d, a, b, c, x(k + 1), S12, 0xE8C7B756    
DO md5_FF WITH c, d, a, b, x(k + 2), S13, 0x242070DB    


DO md5_FF WITH b, c, d, a, x(k + 3), S14, 0xC1BDCEEE    
DO md5_FF WITH a, b, c, d, x(k + 4), S11, 0xF57C0FAF    
DO md5_FF WITH d, a, b, c, x(k + 5), S12, 0x4787C62A    
DO md5_FF WITH c, d, a, b, x(k + 6), S13, 0xA8304613    
DO md5_FF WITH b, c, d, a, x(k + 7), S14, 0xFD469501    
DO md5_FF WITH a, b, c, d, x(k + 8), S11, 0x698098D8    
DO md5_FF WITH d, a, b, c, x(k + 9), S12, 0x8B44F7AF    
DO md5_FF WITH c, d, a, b, x(k + 10), S13, 0xFFFF5BB1    
DO md5_FF WITH b, c, d, a, x(k + 11), S14, 0x895CD7BE    
DO md5_FF WITH a, b, c, d, x(k + 12), S11, 0x6B901122    
DO md5_FF WITH d, a, b, c, x(k + 13), S12, 0xFD987193    
DO md5_FF WITH c, d, a, b, x(k + 14), S13, 0xA679438E    
DO md5_FF WITH b, c, d, a, x(k + 15), S14, 0x49B40821    

DO md5_GG WITH a, b, c, d, x(k + 1), S21, 0xF61E2562    
DO md5_GG WITH d, a, b, c, x(k + 6), S22, 0xC040B340    
DO md5_GG WITH c, d, a, b, x(k + 11), S23, 0x265E5A51    
DO md5_GG WITH b, c, d, a, x(k + 0), S24, 0xE9B6C7AA    
DO md5_GG WITH a, b, c, d, x(k + 5), S21, 0xD62F105D    
DO md5_GG WITH d, a, b, c, x(k + 10), S22, 0x2441453    
DO md5_GG WITH c, d, a, b, x(k + 15), S23, 0xD8A1E681    
DO md5_GG WITH b, c, d, a, x(k + 4), S24, 0xE7D3FBC8    
DO md5_GG WITH a, b, c, d, x(k + 9), S21, 0x21E1CDE6    
DO md5_GG WITH d, a, b, c, x(k + 14), S22, 0xC33707D6    
DO md5_GG WITH c, d, a, b, x(k + 3), S23, 0xF4D50D87    
DO md5_GG WITH b, c, d, a, x(k + 8), S24, 0x455A14ED    
DO md5_GG WITH a, b, c, d, x(k + 13), S21, 0xA9E3E905    
DO md5_GG WITH d, a, b, c, x(k + 2), S22, 0xFCEFA3F8    
DO md5_GG WITH c, d, a, b, x(k + 7), S23, 0x676F02D9    


DO md5_GG WITH b, c, d, a, x(k + 12), S24, 0x8D2A4C8A    

DO&nbsp;md5_HH WITH a, b, c, d, x(k + 5), S31, 0xFFFA3942    
DO md5_HH WITH d, a, b, c, x(k + 8), S32, 0x8771F681    
DO md5_HH WITH c, d, a, b, x(k + 11), S33, 0x6D9D6122    
DO md5_HH WITH b, c, d, a, x(k + 14), S34, 0xFDE5380C    
DO md5_HH WITH a, b, c, d, x(k + 1), S31, 0xA4BEEA44    
DO md5_HH WITH d, a, b, c, x(k + 4), S32, 0x4BDECFA9    
DO md5_HH WITH c, d, a, b, x(k + 7), S33, 0xF6BB4B60    
DO md5_HH WITH b, c, d, a, x(k + 10), S34, 0xBEBFBC70    
DO md5_HH WITH a, b, c, d, x(k + 13), S31, 0x289B7EC6    
DO md5_HH WITH d, a, b, c, x(k + 0), S32, 0xEAA127FA    
DO md5_HH WITH c, d, a, b, x(k + 3), S33, 0xD4EF3085    
DO md5_HH WITH b, c, d, a, x(k + 6), S34, 0x4881D05    
DO md5_HH WITH a, b, c, d, x(k + 9), S31, 0xD9D4D039    
DO md5_HH WITH d, a, b, c, x(k + 12), S32, 0xE6DB99E5    
DO md5_HH WITH c, d, a, b, x(k + 15), S33, 0x1FA27CF8    
DO md5_HH WITH b, c, d, a, x(k + 2), S34, 0xC4AC5665    

DO md5_II WITH a, b, c, d, x(k + 0), S41, 0xF4292244    
DO md5_II WITH d, a, b, c, x(k + 7), S42, 0x432AFF97    
DO md5_II WITH c, d, a, b, x(k + 14), S43, 0xAB9423A7    
DO md5_II WITH b, c, d, a, x(k + 5), S44, 0xFC93A039    
DO md5_II WITH a, b, c, d, x(k + 12), S41, 0x655B59C3    
DO md5_II WITH d, a, b, c, x(k + 3), S42, 0x8F0CCC92    
DO md5_II WITH c, d, a, b, x(k + 10), S43, 0xFFEFF47D    
DO md5_II WITH b, c, d, a, x(k + 1), S44, 0x85845DD1    
DO md5_II WITH a, b, c, d, x(k + 8), S41, 0x6FA87E4F    
DO md5_II WITH d, a, b, c, x(k + 15), S42, 0xFE2CE6E0    
DO md5_II WITH c, d, a, b, x(k + 6), S43, 0xA3014314    


DO md5_II WITH b, c, d, a, x(k + 13), S44, 0x4E0811A1    
DO md5_II WITH a, b, c, d, x(k + 4), S41, 0xF7537E82    
DO md5_II WITH d, a, b, c, x(k + 11), S42, 0xBD3AF235    
DO md5_II WITH c, d, a, b, x(k + 2), S43, 0x2AD7D2BB    
DO md5_II WITH b, c, d, a, x(k + 9), S44, 0xEB86D391    

a = AddUnsigned(a, AA)    
b = AddUnsigned(b, BB)    
c = AddUnsigned(c, CC)    
d = AddUnsigned(d, DD)    
ENDFOR    

ON ERROR &&恢复默认的错误处理    
*************下面是输出16位代码的方法*************     
* RETURN LOWER(WordToHex(a) + WordToHex(b) + WordToHex(c) + WordToHex(d))    
*************下面是输出8位代码的方法*************    
RETURN LOWER(WordToHex(b) + WordToHex(c))     
ENDFUNC    

PROC err_treat    
RETURN    
ENDPROC