package xynu;

import org.junit.Test;

import java.math.BigDecimal;


public class TestPay {

@Test
    public void test01(){

        String str = "234.23";
        Double value = Double.valueOf(str);
        System.out.println(value/100);
    }

    @Test
    public void test02(){

    String str = "3434348";
        Long aLong = Long.parseLong(str);
        BigDecimal bigDecimal = BigDecimal.valueOf(aLong);
        BigDecimal cs = BigDecimal.valueOf(100d);
        BigDecimal divide = bigDecimal.divide(cs);
        System.out.println(divide);
    }

}
