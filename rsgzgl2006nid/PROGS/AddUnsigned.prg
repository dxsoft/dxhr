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
ENDFUN 