#include <stdio.h>
#include <ctype.h>

int main() 
{

   int col = 0;
   int c,bool=1,done=0;
   print()
   while ((c = getchar()) != EOF) 
   {
      if (c == '\n') 
	  {
         col = 0;
         putchar(c);
         bool=1;
         done=0;
      }
      else if(done){
         col++;
         putchar(c);
      }
      else if (isblank(c)&&bool) 
	  {
         putchar('\t');
         bool=0;
         col++;

      }
      else if(isblank(c)){
         if((++col)%4==0){
            bool=1;
         }
      }
      else{
         done=1;
         putchar(c);
         col++;
      }
    }
    return 0;
}  