package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.apache.solr.common.util.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/pay")
public class PayController {

    @Reference(timeout = 300000)
    private AliPayService aliPayService;

    @Autowired
    private IdWorker idWorker;

    @Reference
    private OrderService orderService;
    @RequestMapping(value = "/createNative")
    public Map createNative() {

        System.out.println("进入了吗");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if (payLog != null) {
            return aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");

        } else {
          return   new HashMap<>();
        }

    }

    @RequestMapping(value = "/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        Map map=null;
        int x=0;
        while (true) {

                map = aliPayService.queryPayStatus(out_trade_no);

            //判断map是否为空
            if(map==null){
                //调用查询订单状态服务出现了错误
                result=new Result(false,"查询订单状态出现错误");
                break;
            }
            //1、支付成功
            if(map.get("trade_status")!=null&&map.get("trade_status").equals("TRADE_SUCCESS")){

                //修改订单状态
                orderService.updateOrderStatus(out_trade_no,(String) map.get("trade_no"));
                result=new Result(true,"支付成功");
                break;
            }
            //2、交易撤销或者关闭
            if(map.get("trade_status")!=null&&map.get("trade_status").equals("TRADE_CLOSED")){
                result=new Result(false,"交易撤销或者关闭");
                break;
            }

            x++;
            //让查询等待3秒在继续查询
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(x>10){
                result=new Result(false,"二维码查询超时");
                break;
            }

        }

        return result;
    }
}
