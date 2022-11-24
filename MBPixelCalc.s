	.file	"MBPixelCalc.s"
	.text
	.globl	MBPixelCalc
	.type	MBPixelCalc, @function
MBPixelCalc:
.LFB0:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
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
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	MBPixelCalc, .-MBPixelCalc
	.ident	"GCC: (Ubuntu 7.5.0-3ubuntu1~18.04) 7.5.0"
	.section	.note.GNU-stack,"",@progbits
