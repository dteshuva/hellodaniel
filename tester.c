/* example.c */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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
long powBin(int n){
    long res=2;
    for(int i=2; i<=n; i++){
        res*=2; }
    return res;
}

long calcFrac(double n){
    int count=0;
    long res=0, fiftyEight=288230376151711744;
    double temp=n;
    while(n*10<1){
        n*=10;
        count++;
    }
    count++;
    int firstExp=57-count, c=0;
    double stop=0;
    while((temp-(int)temp)!=0&&c<=800){
      //  printf("n %f \n",temp-(int)temp);
        n*=10;
     //   printf("p %f \n",temp-(int)temp);
      //  printf("hope u die %ld\n",powBin(count+1));
        if((int)n%10==1){
            res+= (fiftyEight/powBin(count));
        }
        count++;
        c++;
        firstExp--;
      //  printf("%f and rounded %d\n",temp,(int)temp);
        //printf("their substraction %f \n",temp-(int)temp);
       // printf("tov %d\n\n",((temp-(int)temp)!=stop));

    }
    return res;
}
long trick(int n){
    long res;
    switch (n) {
        case 1:
            res= 288230376151711744;
            break;
        case 0:
            res=0;
            break;
        case -2:
            res= -576460752303423488;
            break;
        default:
            res= -288230376151711744;
            break;
    }
    return res;
}
int main(int argc, char *argv[]) {
    char *mem;
    char *mem2;
    // conert arguments to double
    double x=strtod(argv[1],&mem);
    double y=strtod(argv[2],&mem2);
    // get rid of frac part, 2.65 -> 2

  //  printf("arguments are %f and %f\n",x,y);
    int xw=(int)x,yw=(int)y;
    double xd=x>=0? (x-xw):-1*(x-xw),yd=y>=0?(y-yw):-1*(y-yw);  // getting the fraction part of the number
     // printf("size: %d\n", sizeof(long));
    double binOfxF= fracToBin(xd,-1,xd),binOfyF= fracToBin(yd,-1,yd); // converting the fractional part of the number to binary
  //  long nX= calcDec(binOfxW),nY= calcDec(binOfyW);
    long nX= trick(xw),nY= trick(yw);
    if(binOfxF>0)
    nX+= calcFrac(binOfxF);
    if(binOfyF>0)
    nY+= calcFrac(binOfyF);
    if(xw==0&&x<0) nX=-1*nX;
    if(yw==0&&y<0) nY=-1*nY;
    printf("x is %ld and y is %ld\n",nX,nY);


    extern int MBPixelCalc(long,long);
    // printf("x: %ld y: %ld\n",nX,nY);
    printf("MBPixelCalc() returned %d.\n", MBPixelCalc(nX,nY));
    return 0;
}