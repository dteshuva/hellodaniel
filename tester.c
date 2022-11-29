/* example.c */
#include <stdio.h>
#include <stdlib.h>
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
double pow(double x,double y){
    double result=1/x;
    for(int i=2; i<=-1*y; i++) result*=(1/x);
    return result;
}
double fracToBin(double n,int k, double orNum){ //k=-1 and change it so ornum is a paramater
    if(orNum==0) return 0;
    double curNum=2.0*n,rem=((int)curNum)%2;
  //  printf("k: %d * curNum: %f * rem: %f ",k,curNum,rem);
    if(curNum==1||curNum==orNum){   return rem*pow(10,k);}
   // printf("%f +frac()\n",rem*pow(10,k));
    return rem*pow(10,k)+ fracToBin(curNum-rem,k-1,orNum);
}

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
    // conert arguments to double
    double x=strtod(argv[1],&mem);
    double y=strtod(argv[2],&mem2);
    // get rid of frac part, 2.65 -> 2

    printf("arguments are %f and %f\n",x,y);
    int xw=(int)x,yw=(int)y;
    double xd=x-xw,yd=y-yw;  // getting the fraction part of the number

    long binOfxW= convert(xw),binOfyW= convert(yw); // converting decimal part to binary
    double binOfxF= fracToBin(xd,-1,xd),binOfyF= fracToBin(yd,-1,yd); // converting the fractional part of the number to binary
    long nX,nY;

/*    char sX= x>=0? '0':'1',sY=y>=0?'0':'1';
    char s1[20],s2[20],s3[20],s4[20];
    printf("decimal of x: %ld and fraction is: %f\n",binOfxW,binOfxF);
    printf("decimal of y: %ld and fraction is: %f\n",binOfyW,binOfyF);
    sprintf(s1,"%ld",binOfxW);
    sprintf(s2,"%ld",binOfxF);
    sprintf(s3,"%ld",binOfyW);
    sprintf(s4,"%ld",binOfyF);
    strcat(s1,s2);
    strcat(s3,s4);
    long nX= atoi(s1),nY= atoi(s3); // combining frac and dec part of x and y  */
    extern int MBPixelCalc(long,long);
     printf("x: %ld y: %ld\n",nX,nY);
    printf("MBPixelCalc() returned %d.\n", MBPixelCalc(nX,nY));
    return 0;
}