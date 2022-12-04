	.text
	.globl	MBPixelCalc
MBPixelCalc:
	pushq	%rbp
	movq	%rsp, %rbp
	movsd	%xmm0, -40(%rbp)
	movsd	%xmm1, -48(%rbp)
	pxor	%xmm0, %xmm0
	movsd	%xmm0, -24(%rbp)
	pxor	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	movl	$0, %edi
	jmp	.L2
.L5:
	movsd	-24(%rbp), %xmm0
	mulsd	-24(%rbp), %xmm0
	movsd	-16(%rbp), %xmm1
	mulsd	-16(%rbp), %xmm1
	subsd	%xmm1, %xmm0
	movsd	-40(%rbp), %xmm1
	addsd	%xmm1, %xmm0
	movsd	%xmm0, -8(%rbp)
	movsd	-24(%rbp), %xmm0
	addsd	%xmm0, %xmm0
	mulsd	-16(%rbp), %xmm0
	movsd	-48(%rbp), %xmm1
	addsd	%xmm1, %xmm0
	movsd	%xmm0, -16(%rbp)
	movsd	-8(%rbp), %xmm0
	movsd	%xmm0, -24(%rbp)
	addl	$1, %edi
.L2:
	movsd	-24(%rbp), %xmm0
	movapd	%xmm0, %xmm1
	mulsd	-24(%rbp), %xmm1
	movsd	-16(%rbp), %xmm0
	mulsd	-16(%rbp), %xmm0
	addsd	%xmm1, %xmm0
	movsd	.LC1(%rip), %xmm1
	ucomisd	%xmm0, %xmm1
	jb	.L3
	cmpl	$999, %edi
	jle	.L5
.L3:
	movl	%edi, %eax
	popq	%rbp
	ret
.LC1:
	.long	0
	.long	1074790400
