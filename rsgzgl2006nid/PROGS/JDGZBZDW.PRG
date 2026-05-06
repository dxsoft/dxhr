func jdgzbzdw
     para v_nd 
        v_nd=left(v_nd,6)
		c_tbnd=''
		sele jdgzbz
		set filt to
		go top
		v_mintbnd='199501'
		do while .t.
		   loca for tbnd=v_nd
		   if found()
		      c_tbnd=tbnd 
		      exit
		   else
		      loca for tbnd>v_mintbnd and tbnd<v_nd
		      if found()
		         v_mintbnd=tbnd
		      else
		         exit
		      endi
		   endi   
		endd
		if empt(c_tbnd)
		   c_tbnd=v_mintbnd
		endi   
retu c_tbnd
