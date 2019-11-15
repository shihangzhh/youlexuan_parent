package xynu.shihang;

import org.junit.Test;

public class CreateCode {

    @Test
    public void test01(){
 //y用于测试产生六位数的验证码


     while (true){
         int i =   (int )(Math.random()*900000)+100000;
         String str=  String.valueOf(i);
         if (str.length() <6){
             System.out.println(i);
             return;
         }

     }

    }

    @Test
    public  void test02(){

        int a= 5;
        int b = 6;
        boolean flag =  b<a;
       if (!flag){
           System.out.println("bbbb");
       }
    }
}
