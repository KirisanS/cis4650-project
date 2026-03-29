* C-Minus Compilation to TM Code
* File: tests/code_tests/basic.cm
* Standard prelude: 
  0:    LD 6 0(0)	load gp with maxaddress
  1:   LDA 5 0(6)	copy gp to fp
  2:    ST 0 0(0)	clear location 0
* Jump around i/o routines here
* code for input routine
  4:    ST 0 -1(5)	store return
  5:    IN 0 0(0)	input
  6:    LD 7 -1(5)	return to caller
* code for output routine
  7:    ST 0 -1(5)	store return
  8:    LD 0 -2(5)	load output value
  9:   OUT 0 0 0	output
 10:    LD 7 -1(5)	return to caller
  3:   LDA 7 7(7)	jump around i/o code
* End of standard prelude
* process function: main
* jump around function body here
 12:    ST 0 -1(5)	save return address
* -> compound statement
* processing local var: x
* processing local var: y
* -> op
* -> id
* looking up id: x
 13:   LDA 0 -2(5)	load id address
* <- id
 14:    ST 0 -4(5)	op: push left
* -> op
* -> id
* looking up id: x
 15:    LD 0 -2(5)	load id value
* <- id
 16:    ST 0 -4(5)	op: push left
* -> id
* looking up id: y
 17:    LD 0 -3(5)	load id value
* <- id
 18:    LD 1 -4(5)	op: load left
 19:   ADD 0 1 0	op +
* <- op
 20:    LD 1 -4(5)	op: load left
 21:    ST 0 0(1)	assign: store value
* <- op
* <- compound statement
 22:    LD 7 -1(5)	return to caller
 11:   LDA 7 11(7)	jump around fn body
* <- function declaration
* Finale
 23:    ST 5 0(5)	push ofp
 24:   LDA 5 0(5)	push frame
 25:   LDA 0 1(7)	load ac with ret ptr
 26:   LDA 7 -15(7)	jump to main loc
 27:    LD 5 0(5)	pop frame
 28:  HALT 0 0(0)	
