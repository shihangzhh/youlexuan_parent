package com.offcn.page.service;

public interface ItemPageService {


    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */

    public boolean genItemHtml(Long goodsId);

    //根据传入的静态网页的id删除静态网页
    public void deleteHtml(Long [] ids);

}
