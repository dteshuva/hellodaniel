#include <stdio.h>
int MBPixelCalc(long x, long y){
    long xS=0,yS=0;
    int iteration=0,maxIt=1000;
    while(xS*xS+yS*yS<=4 && iteration<maxIt){
        long tempx=xS*xS-yS*yS+x;
        y=2*x*y+yS;
        x=tempx;
        iteration++;
    }
    return iteration;
}//
// Created by user on 24/11/2022.
//
