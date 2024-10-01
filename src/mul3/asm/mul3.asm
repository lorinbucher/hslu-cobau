DEFAULT REL		; defines relative addresses are used; is a NASM command

; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit
; export entry point
global _start
    LENGTH EQU 64   ; definition constant LENGTH (buffer length), max length of an int
    char_offset EQU 48  ; ASCII char offset
    Section .data
        WELCOME: db 'Please enter an integer value:', 13, 10	; defines the string constant
                                        ; don’t add CR (10) in Linux
        WEL_LEN: EQU $-WELCOME		        ; Reads the length of WELCOME ino WEL_LN

        OUTPUT: db 'The result of value * 3 is: '	; defines the string constant
                                        ; don’t add CR (10) in Linux
        OUT_LEN: EQU $-OUTPUT		        ; Reads the length of OUTPUT ino OUT_LN

        ERROR: db 'Please enter an INTEGER value!', 13, 10	; defines the string constant
                                              ; don’t add CR (10) in Linux
        ERR_LEN: EQU $-ERROR		        ; Reads the length

section .bss		; defines start of section of uninitialized data
    align 8            ; align to 8 bytes (for 64-bit machine)
    BUFFER resb LENGTH  ; buffer (64 bytes)
    BUF2 resb LENGTH

section .text	        ; defines start of code section

_start:
    mov rdi, WELCOME    ; copy address of variable BUFFER into register rdi
    mov rsi, WEL_LEN    ; register rax contains the number of typed char, copy value of rax into register rsi
    call _write         ; now calls function _write to write to console (stdin)
    mov r13,0
    ; set up reading from console
    mov rdi, BUFFER     ; copy address of variable BUFFER into register rdi
    mov rsi, LENGTH     ; copy value of LENGTH into register rsi
    call _read		    ; now calls function _read to read from console (stdin)

    mov r13, rax        ; set up r13 as a counter:
    ;sub r13,1             ; remove end of line character
    mov r14, 0          ; use r14 as position pointer in buffer
    mov r10,0           ; create a 0 storage for the input int
    ;mov r9, r13
    mov r11, 10          ; decimal multiplier


    ;mov r15, BUFFER     ; copy address of variable BUFFER into register rdi
    movzx r8, byte[BUFFER+r14]              ; use r8 as storage for in process result
    cmp r8, 0x2d       ; check first char for being a -
    je  negative_input
    ; the char in r8 needs to become an int
    sub r8, char_offset         ;
    ;check if it is in the range of an int: ASSUMPTION if the first one is an int the others are too
    cmp r8,0
    jl error_exit
    cmp r8,9
    jg error_exit

    add r14,1
    add r10, r8
    cmp r14, r13       ; compare the current buffer pointer to the number of elements in the buffer
    je mul3
positive_input:
    movzx r8, byte[BUFFER+r14]
    cmp r8, 10
    je mul3
    cmp r8,13
    je mul3
    sub r8, char_offset
    imul r10, r11
    add r10, r8
    add r14,1
    cmp r14, r13
    je mul3
    jmp positive_input

negative_input:             ; r14=0, r8=-
    sub r13,1
        add r14,1
        movzx r8, byte[BUFFER+r14]
        sub r8, char_offset
        cmp r8,0
        jl error_exit
        cmp r8,9
        jg error_exit

        sub r10, r8
        add r14, 1
        cmp r14, r13
        jge mul3
    neg_loop:
            movzx r8, byte[BUFFER+r14]
            imul r10, r11
            sub r8, char_offset
            sub r10, r8
            add r14,1
            cmp r14, r13
            jl neg_loop

mul3: ; multiply the int *3
    imul rax, r10, 3     ; put the result in rax to use in division
    mov r12, 0         ; division counter and length of output
    mov rdx, 0
    cmp rax, 0
    jg out_pos
    cmp rax, 0
    jl out_neg
    jmp finish_up ; catch 0

out_pos:
    cmp rax, 9
    jle finish_up
    jmp output_loop

out_neg:
    mov byte[BUF2+r12], 0x2d  ; set a - at the first position
    inc r12
    mov r10, 0
    sub r10, rax
    mov rax, r10
    cmp rax, 9
    jle finish_up
    jmp output_loop

output_loop:
    mov rbx, 10
    idiv rbx        ; divide rax by rbx
    add rdx, char_offset         ; get the remainder of the division and turn it into a char
    mov byte[BUF2+r12], dl
    inc r12
    cmp rax, 9
    jle finish_up
    jmp output_loop

finish_up:
    add rax, char_offset
    mov byte[BUF2+r12], al
    mov r15, r12
    inc r15
    mov r13, 0

fill_the_buffer:
    mov al, byte[BUF2+r12],
    mov byte[BUFFER+r13], al
    inc r13
    dec r12
    cmp r13, r15
    jl fill_the_buffer

write:
    mov rdi, OUTPUT
    mov rsi, OUT_LEN
    call _write
    mov rdi, BUFFER     ; copy address of variable BUFFER into register rdi
    mov rsi, r15        ; register rax contains the number of typed char, copy value of rax into register rsi
    call _write         ; now calls function _write to write to console (stdin)

; exit program with exit code 0
exit:
        mov   rdi, 0                ; first parameter: set exit code
        call  _exit                 ; call function

error_exit:
        mov rdi, ERROR
        mov rsi, ERR_LEN
        call _write
        mov   rdi, 1                ; first parameter: set exit code
        call  _exit                 ; call function
