DEFAULT REL		; defines relative addresses are used; is a NASM command

; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit

; export entry point
global _start

LENGTH equ 64           ; definition of the buffer length
CHAR_OFFSET equ '0'     ; definition of the offset to convert between ASCII and digits
NEGATIVE_NUM equ '-'    ; definition of the ASCII character for negative numbers

; defines the start the section of string constants
section .data
    alignb 8                                            ; align to 8 bytes (for 64-bit machine)

    CRLF:       db  13, 10                              ; defines the string for a new line (CRLF)
    CRLF_LEN:   equ $-CRLF                              ; reads the length of the new line text into CRLF_LEN

    TXT_IN:     db 'Please enter an integer value: '    ; defines the string constant for the input text
    TXT_IN_LEN: equ $-TXT_IN                            ; reads the length of the input text into TXT_IN_LEN

    TXT_OUT:     db 'The result of value * 3 is: '      ; defines the string constant for the output text
    TXT_OUT_LEN: equ $-TXT_OUT                          ; reads the length of the output text into TXT_OUT_LEN

; defines the start of the section of uninitialized data
section .bss
    alignb 8                ; align to 8 bytes (for 64-bit machine)

    BUF_IN resb LENGTH      ; input buffer (64 bytes)
    BUF_TMP resb LENGTH     ; temporary buffer (64 bytes)
    BUF_OUT resb LENGTH     ; output buffer (64 bytes)

; defines the start of the code section
section .text

_start:
    mov rdi, TXT_IN                     ; copy the address of the constant TXT_IN into the register rdi
    mov rsi, TXT_IN_LEN                 ; write the length of TXT_IN into the register rsi
    call _write                         ; writes the input text to the console (STDOUT)

    mov rdi, CRLF                       ; copy the address of the constant CRLF into the register rdi
    mov rsi, CRLF_LEN                   ; write the length of CRLF into the register rsi
    call _write                         ; writes the new line to the console (STDOUT)

    mov rdi, BUF_IN                     ; copy the address of variable BUFFER into the register rdi
    mov rsi, LENGTH                     ; copy the length of the input buffer into the register rsi
    call _read                          ; reads the input from the console (STDIN)

    mov r8, 0                           ; initialize the input number register
    mov r9, 0                           ; initialize the input character counter register
    mov r14, 0                          ; initialize the pos/neg switch

    jmp parse_input_digit               ; jump to the loop parsing the input digits

parse_input_digit:
    movzx rcx, byte [BUF_IN + r9]       ; load the next character from the input buffer into register rcx
    inc r9                              ; increment the input character counter

    cmp rcx, NEGATIVE_NUM               ; compare the value with the dash character
    je negative_switch
    ;cmp rcx, NEGATIVE_NUM
    ;je parse_input_digit                ; jump to the next character if the character is a dash

    sub rcx, CHAR_OFFSET                ; convert the ASCII character to a numeric value

    cmp rcx, 0                          ; compare the numeric value with 0
    jl multiply_by_3                    ; exit the parsing of the input if the character is not a number

    cmp rcx, 9                          ; compare the numeric value with 9
    jg multiply_by_3                    ; exit the parsing of the input if the character is not a number

    imul r8, 10                         ; multiply the current input number by 10
    add r8, rcx                         ; add the numeric value to the input number

    jmp parse_input_digit               ; continue with the next character

negative_switch:
    mov r14, 1
    jmp parse_input_digit

multiply_by_3:
    imul rax, r8, 3                     ; multiply the input number by three and store the result into register rax

    mov r11, 10                         ; initialize the divisor to convert the output number
    mov r12, 0                          ; initialize the temporary buffer position counter
    mov r13, 0                          ; initialize the output buffer position counter
    cmp r14, 0                          ; check if input was negative
    jg add_neg_sign
    jmp convert_output_digit            ; jump to the loop converting the output number

add_neg_sign:
    mov byte [BUF_OUT + r13], NEGATIVE_NUM
    inc r13
    jmp convert_output_digit

convert_output_digit:
    mov rdx, 0                          ; reset the register holding the rest to 0
    idiv r11                            ; divide the result by 10, to get the next digit

    add rdx, CHAR_OFFSET                ; convert the numeric value to an ASCII character
    mov byte [BUF_TMP + r12], dl        ; load the digit into the temporary buffer
    inc r12                             ; increment the position in the temporary buffer

    cmp rax, 0                          ; check if the conversion is finished
    je fill_output_buffer               ; jump to fill the output buffer

    jmp convert_output_digit            ; continue with the next character

fill_output_buffer:
    dec r12                             ; decrement the position in the temporary buffer

    mov al, byte [BUF_TMP + r12]        ; load the next digit from the temporary buffer
    mov byte [BUF_OUT + r13], al        ; load the next digit into the output buffer
    inc r13                             ; increment the position in the output buffer

    cmp r12, 0                          ; check if the output buffer is filled
    je _end                             ; jump to write the result to the console

    jmp fill_output_buffer              ; continue with the next position in the output buffer

_end:
    mov rdi, TXT_OUT                    ; copy the address of the constant TXT_OUT into the register rdi
    mov rsi, TXT_OUT_LEN                ; write the length of TXT_OUT into the register rsi
    call _write                         ; writes the output text to the console (STDOUT)

    mov rdi, BUF_OUT                    ; copy the address of the constant BUF_OUT into the register rdi
    mov rsi, r13                        ; write the length of the output buffer into the register rsi
    call _write                         ; writes the output text to the console (STDOUT)

    mov rdi, CRLF                       ; copy the address of the constant CRLF into the register rdi
    mov rsi, CRLF_LEN                   ; write the length of CRLF into the register rsi
    call _write                         ; writes the new line to the console (STDOUT)

    jmp exit                            ; jump to exit the program

; exit program with exit code 0
exit:
    mov rdi, 0                          ; first parameter: set exit code
    call _exit                          ; call the exit function
