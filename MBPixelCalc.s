.section .data

.section .text
.globl MBPixelCalc

MBPixelCalc:
    movq    $0, %rdx
    movq    $0, %rcx
    movq    $0, %rax
.L2:
    movq    $0, %r8
    addq    %rdx, %r8
    imulq   %rdx, %r8
    addq    %rdi, %r8
    movq    $0, %r9
    addq    %rcx, %r9
    imulq   %rcx, %r9
    subq    %r9, %r8
    imulq   %rdi, %rsi
    imul    $2, %rsi
    addq    %rcx, %rsi
    movq    %r8, %rdi
    incq    %rax
    movq    $0, %r10
    addq    %rdx, %r10
    imulq   %rdx, %r10
    movq    $0, %r11
    addq    %rcx, %r11
    imulq   %rcx, %r11
    addq    %r11, %r10
    cmpq    $4, %r10
    jg  .L3
    cmpq $1000, %rax
    jl  .L2
.L3:
    ret
    