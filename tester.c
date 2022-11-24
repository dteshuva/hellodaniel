/* example.c */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

// function gets a whole number and returns a long that represents the number in binary
long convert(int n) {
    long bin = 0;
    int rem, i = 1;

    while (n!=0) {
        rem = n % 2;
        n /= 2;
        bin += rem * i;
        i *= 10;
    }

    return bin;
}
double fracToBin(double n,int k){ //k=-1 and change it so ornum is a paramater
    double orNum=n,curNum=2*n,rem=((int)curNum)%2;
    if(curNum==1||curNum==orNum){ return rem+ pow(10,k);}
    return rem+ pow(10,k)+ fracToBin(curNum-rem,k-1);
}
/*
char* convertFrac(double n){
    char bin[]="";
    int decPart;
    double fracPart;
    char on='1',zer='0';
    while(n!=0){
        n*=2;
        decPart=(int)n;
        fracPart=n-decPart;
        if(decPart==1){ strncat(bin,&on,1);}
        else {strncat(bin,&zer,1); }

        n=fracPart;
    }
    return bin;
} */
// function receives a long and returns how many shifts(divide by 10) are needed, so it's the form of 1.frac
int count(long n){
    if((int)n==1)
        return 0;
    int cn=0;
    while((int)n!=1){
       cn++;
       n/=10;
    }
    return cn;
}
int main(int argc, char *argv[]) {
    char *mem;
    char *mem2;
    printf("lets go");
    // conert arguments to double
    double x=strtod(argv[1],&mem);
    double y=strtod(argv[2],&mem2);
    // get rid of frac part, 2.65 -> 2
    printf("argiments are %f and %f",x,y);
    int xw=(int)x,yw=(int)y;
    double xd=x-xw,yd=y-yw;  // getting the fraction part of the number
    long binOfxW= convert(xw),binOfyW= convert(yw); // converting decimal part to binary
    double binOfxF= fracToBin(xd,-1),binOfyF= fracToBin(yd,-1); // converting the fractional part of the number to binary
    int xShift= count(binOfxW),yShift=count(binOfyW); // getting the number of shifts, hence Exp
    long double xT=binOfxW+binOfxF, yT=binOfyW+binOfyF; // combining decimal and fraction to get full number in binary
    for(int i=1; i<=xShift; i++){ xT/=10; }  // shifting the binary point- if all code works try doing bit shift instead
    for(int i=1; i<=yShift; i++){ yT/=10; }
    int xExp=15+xShift,yExp=15+yShift; // bias is 2^4-1=15
    long expBin= convert(xExp),eypBin= convert(yExp); // getting binary represntation of the exponent
    long double fracX=xT-(int)xT,fracY=yT-(int)yT; // getting the frac part of the number after shifting
    while(fracX!=(int)fracX){ fracX*=10;} //converting the frac to an integer to an integer
    while(fracY!=(int)fracY){ fracY*=10;}
    long fractX=(int)fracX,fractY=(int)fracY; // moving the frac to a type long
    char sX= x>=0? '0':'1',sY=y>=0?'0':'1';
    char s1[20],s2[20],s3[20],s4[20];
    sprintf(s1,"%ld",expBin);
    sprintf(s2,"%ld",fractX);
    sprintf(s3,"%ld",eypBin);
    sprintf(s4,"%ld",fractY);
    strcat(s1,s2);
    strcat(s3,s4);
    long nX= atoi(s1),nY= atoi(s3); // combining frac and dec part of x and y
	extern int MBPixelCalc(long,long);

   printf("MBPixelCalc() returned %d.\n", MBPixelCalc(nX,nY));
  return 0;
}
