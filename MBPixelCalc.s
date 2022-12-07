	.globl	MBPixelCalc
MBPixelCalc:
	movsd	%xmm0, %xmm2
	pxor	%xmm0, %xmm0
	movsd	%xmm0, %xmm4
	movsd	%xmm0, %xmm5
	movsd	%xmm1, %xmm3
	movl	$0, %edi
	jmp	.L1
.L2:
	movsd	%xmm4, %xmm0
	mulsd	%xmm4, %xmm0
	movsd	%xmm5, %xmm1
	mulsd	%xmm5, %xmm1
	subsd	%xmm1, %xmm0
	movsd	%xmm2, %xmm1
	addsd	%xmm1, %xmm0
	movsd	%xmm0, %xmm6
	movsd	%xmm4, %xmm0
	addsd	%xmm0, %xmm0
	mulsd	%xmm5, %xmm0
	movsd	%xmm3, %xmm1
	addsd	%xmm1, %xmm0
	movsd	%xmm0, %xmm5
	movsd	%xmm6, %xmm0
	movsd	%xmm0, %xmm4
	incl	%edi
.L1:
	movsd	%xmm4, %xmm0
	movsd	%xmm0, %xmm1
	mulsd	%xmm4, %xmm1
	movsd	%xmm5, %xmm0
	mulsd	%xmm5, %xmm0
	addsd	%xmm1, %xmm0
	movsd	.address(%rip), %xmm1
	comisd	%xmm1, %xmm0
	ja	.L3
	cmpl	$1000, %edi
	jl	.L2
.L3:
	movl	%edi, %eax
	ret
.address:
	.long	0
	.long	1074790400
