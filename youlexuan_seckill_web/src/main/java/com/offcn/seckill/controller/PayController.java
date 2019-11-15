package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/pay")
public class PayController {


    @Reference
    private SeckillOrderService seckillOrderService;

    @Reference(timeout = 30000)
    private AliPayService aliPayService;

    @RequestMapping(value = "/createNative")
    public  Map createNative(){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //从缓存中查询秒杀商品
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        System.out.println("用户id是"+userId);
        if (seckillOrder != null){

          long  fen = (long) (seckillOrder.getMoney().doubleValue()*100);//金额(分)
            return  aliPayService.createNative(seckillOrder.getId()+"",fen+"");

        }
        return new HashMap();
    }

    @RequestMapping(value = "/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;

        int x = 0;
        while (true){

            Map<String,String> map = aliPayService.queryPayStatus(out_trade_no);
            if(map == null){
                result = new Result(false,"支付出错");
                break;
            }
                 //1 支付成功
            if(map.get("trade_status")!=null&&map.get("trade_status").equals("TRADE_SUCCESS")){

                result = new Result(true,"支付成功");
                //将秒杀结果保存到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no),map.get("trade_no"));
                break;
            }
            //2、交易撤销或者关闭
            if(map.get("trade_status")!=null&&map.get("trade_status").equals("TRADE_CLOSED")){
                result=new Result(false,"交易撤销或者关闭");
                break;
            }
              x++;
            if (x ==10){
                result = new Result(false,"小主，二维码超时了啊");
                seckillOrderService.deleteOrderFromRedis(userId,out_trade_no);
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

        return  result;
    }
}
