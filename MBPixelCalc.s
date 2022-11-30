.section .data

.section .text
.globl MBPixelCalc

MBPixelCalc:
.LFB0:
	pushq	%rbp
	movq	%rsp, %rbp
	movq	%rdi, -40(%rbp)
	movq	%rsi, -48(%rbp)
	movq	$0, -24(%rbp)
	movq	$0, -16(%rbp)
	movl	$0, -32(%rbp)
	movl	$1000, -28(%rbp)
	jmp	.L2
.L4:
	movq	-24(%rbp), %rax
	imulq	-24(%rbp), %rax
	movq	%rax, %rdx
	movq	-16(%rbp), %rax
	imulq	-16(%rbp), %rax
	subq	%rax, %rdx
	movq	-40(%rbp), %rax
	addq	%rdx, %rax
	movq	%rax, -8(%rbp)
	movq	-40(%rbp), %rax
	addq	%rax, %rax
	imulq	-48(%rbp), %rax
	movq	%rax, %rdx
	movq	-16(%rbp), %rax
	addq	%rdx, %rax
	movq	%rax, -48(%rbp)
	movq	-8(%rbp), %rax
	movq	%rax, -40(%rbp)
	addl	$1, -32(%rbp)
.L2:
	movq	-24(%rbp), %rax
	imulq	-24(%rbp), %rax
	movq	%rax, %rdx
	movq	-16(%rbp), %rax
	imulq	-16(%rbp), %rax
	addq	%rdx, %rax
	cmpq	$4, %rax
	jg	.L3
	movl	-32(%rbp), %eax
	cmpl	-28(%rbp), %eax
	jl	.L4
.L3:
	movl	-32(%rbp), %eax
	popq	%rbp
	ret
