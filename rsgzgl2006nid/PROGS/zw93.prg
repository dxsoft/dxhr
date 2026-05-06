PARAMETERS tcGwflbm

LOCAL v_zwbm,v_rznx

SELECT ryzwbh
SET FILTER TO LEFT(zwbm,2)=tcGwflbm
SET ORDER TO srny
GO TOP IN ryzwbh

v_zwbm=""
DO WHILE !EOF("ryzwbh")
    IF ryzwbh.srny<"1993.10"
        v_zwbm=ryzwbh.zwbm
        v_rznx=1993-val(left(ryzwbh.srny,4))+1
    ENDIF
    LOOP
ENDDO
