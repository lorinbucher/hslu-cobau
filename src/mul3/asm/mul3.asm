; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit

; export entry point
global _start

section .text

_start:

; implement multiplication by 3 (milestone 1)


; exit program with exit code 0
exit:       mov   rdi, 0                ; first parameter: set exit code
            call  _exit                 ; call function
