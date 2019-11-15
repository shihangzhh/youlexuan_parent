package com.offcn.pay.service;

import com.offcn.pojo.TbPayLog;

import java.util.Map;

public interface AliPayService {

    /**
     * 生成二维码
     * @param out_trade_no  订单号
     * @param total_fee  金额(分)
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no 支付流水号
     * @return
     */

    public Map queryPayStatus(String out_trade_no);


}
