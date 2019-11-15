package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayAcquirePrecreateRequest;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map<String,String> map = new HashMap<String,String>();
        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
//换算付款金额  元
        Long aLong = Long.parseLong(total_fee);
        BigDecimal bigDecimal = BigDecimal.valueOf(aLong);
        BigDecimal cs = BigDecimal.valueOf(100d);
        BigDecimal all_monty = bigDecimal.divide(cs);
        System.out.println("下单金额为"+all_monty.doubleValue());

        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"subject\":\"测试商品001\"," +
                "    \"store_id\":\"NJ_001\"," +
                "    \"timeout_express\":\"2m\"," +
                "    \"total_amount\":\""+all_monty.doubleValue()+"\"" +
                "  }"); //设置业务参数

        try {
            //获取预订单响应
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从响应对象中获取响应结果
            //获取响应码
            String code = response.getCode();
            System.out.println("响应码为"+code);
            //获取响应体 里面包含很多的内容
            String body = response.getBody();
            System.out.println("响应体中的内容包含"+body);

            //10000表示接口调用成功
            if (code.equals("10000")){
            map.put("qrcode",response.getQrCode());
            map.put("out_trade_no",response.getOutTradeNo());
            map.put("total_fee",total_fee);
                    /*System.out.println("qrCode"+response.getQrCode());
                    System.out.println("out_trade_no"+response.getOutTradeNo());
                System.out.println("total_fee"+total_fee);*/
            }else {
                System.out.println("接口调用失败");
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 交易查询接口 alipay.trade.query
     * @param out_trade_no 支付流水号
     * @return
     */

    @Override
    public Map queryPayStatus(String out_trade_no) {

        Map<String,String> map = new HashMap<String, String>();
        //创建支付查询对象
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String code = response.getCode();
            System.out.println("响应码为"+code);

            String body = response.getBody();
            System.out.println("响应体为"+body);

            if (code.equals("10000")){

                map.put("out_trade_no", response.getOutTradeNo());
                map.put("trade_status", response.getTradeStatus());
                System.out.println("支付状态为"+response.getTradeStatus());
                map.put("trade_no",response.getTradeNo());
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }


}
