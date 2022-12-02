.section .data

.section .text
.globl MBPixelCalc

MBPixelCalc:
    movq    $0, %r8  /* Xs */
    movq    $0, %r9  /* Ys */
    movq    $0, %r11 /* iteration */

.L2:
    movq    %r8, %rax
    imulq   %rax
    sal     $6, %rdx
    sar     $58, %rax
    movq    %rax, %r10 /* tempx */
    movq    %r9, %rax
    imulq   %rax
    sal     $6, %rdx
    sar     $58, %rax
    subq    %rax, %r10
    addq    %rdi, %r10
    imulq   %rdi, %rsi  /* y=y*x */
    imulq    $2, %rsi  /* y=2*y*x */
    addq    %r10, %rsi  /* y=2*y*x+tempx */
    movq    %r10, %rdi
    addq    $1, %r11 /* iteration++ */
    movq    %r8, %rax
    imulq   %rax
    sal     $6, %rdx
    sar     $58, %rax
    movq    %rax, %rcx
    movq    %r9, %rax
    imulq   %rax
    sal     $6, %rdx
    sar     $58, %rax
    addq    %rax, %rcx
    cmpq    $4, %rcx
    jl  .L3
    cmpq $1000, %r11
    jg  .L2
.L3:
    movq %r11, %rax
    ret
    