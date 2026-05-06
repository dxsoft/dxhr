func czrz
para czlb

priv nWdb
nWdb=sele()

if used('czrz')
   sele czrz
else
   sele 0
   use czrz
endif
insert into czrz (uid, gn, wj, sj) value (m.yhh, czlb, dbf(nWdb), datetime())
use
sele(nWdb)

return .t.   