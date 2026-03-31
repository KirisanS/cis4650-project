* C-Minus Compilation to TM Code
* File: tests/code_tests/basic.cm
* Standard prelude: 
  0:    LD 6,0(0)	load gp with maxaddress
  1:   LDA 5,0(6)	copy gp to fp
  2:    ST 0,0(0)	clear location 0
* Jump around i/o routines here
* code for input routine
  4:    ST 0,-1(5)	store return
  5:    IN 0,0,0	input
  6:    LD 7,-1(5)	return to caller
* code for output routine
  7:    ST 0,-1(5)	store return
  8:    LD 0,-2(5)	load output value
  9:   OUT 0,0,0	output
 10:    LD 7,-1(5)	return to caller
  3:   LDA 7,7(7)	jump around i/o code
* End of standard prelude
* processing local var: x
* process function: main
* jump around function body here
 12:    ST 0,-1(5)	save return address
* -> compound statement
* -> op
* -> id
* looking up id: x
 13:   LDA 0,0(6)	load id address
* <- id
 14:    ST 0,-2(5)	op: push left
* -> constant
 15:   LDC 0,5(0)	load const
* <- constant
 16:    LD 1,-2(5)	op: load left
 17:    ST 0,0(1)	assign: store value
* <- op
* -> call of function: output
* -> id
* looking up id: x
 18:    LD 0,0(6)	load id value
* <- id
 19:    ST 0,-4(5)	store arg val
 20:    ST 5,-2(5)	push ofp
 21:   LDA 5,-2(5)	push frame
 22:   LDA 0,1(7)	load ac with return pointer
 23:   LDA 7,-17(7)	jump to output loc
 24:    LD 5,0(5)	pop frame
* <- call
* <- compound statement
 25:    LD 7,-1(5)	return to caller
 11:   LDA 7,14(7)	jump around fn body
* <- function declaration
* Finale
 26:    ST 5,-1(5)	push ofp
 27:   LDA 5,-1(5)	push frame
 28:   LDA 0,1(7)	load ac with ret ptr
 29:   LDA 7,-18(7)	jump to main loc
 30:    LD 5,0(5)	pop frame
* End of execution.
 31:  HALT 0,0,0	
