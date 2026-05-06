PROCEDURE cc

SET PROCEDURE TO d:\rsgzgl2006\progs\zwmc
h="d:\rsgzgl2006"
SELECT dwbm,grbm,zwbm1,zwmc(zwbm1),zwbm2,zwmc(zwbm2),jbgzjb1,jbgzjb2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(jbgzjb1)<=VAL(jbgzjb2) 
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,jbgzjb1,jbgzjb2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(jbgzjb1)<=VAL(jbgzjb2)
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc1)>=VAL(zwgzdc2)
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc1)<=VAL(zwgzdc2)
SET PROCEDURE TO d:\rsgzgl2006\progs\jbscope addi
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc2)<=VAL(LEFT(jbscope(zwbm2),2))
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc2)<=VAL(LEFT(jbscope(zwbm2),2)) ORDER BY zwbm2
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc1)<VAL(zwgzdc2)
SELECT dwbm,grbm,zwbm1,zwgw1,zwbm2,zwgw2,zwgzdc1,zwgzdc2 FROM hisbase WHERE jslb="职务变化" AND jsnf+jsyf>"2006.06" AND VAL(zwgzdc2)<VAL(LEFT(jbscope(zwbm2),2)) ORDER BY zwbm2