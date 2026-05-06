PROCEDURE setsel

PARAMETERS nod,chk

nod.checked=chk

PRIVATE node
if !isnull(nod.child)
    node=nod.child.firstsibling
	do while !isnull(node)
	    setsel(node,chk)
	    Node=node.next
	enddo
endif