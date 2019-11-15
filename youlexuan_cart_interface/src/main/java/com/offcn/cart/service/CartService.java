package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService  {

    /**
     *添加商品到购物车
     * @param srcCartList  原购物车商品集合
     * @param itemId  sku编号
     * @param num   购买数量
     * @return
     */

    public List<Cart> addGoodToCartList(List<Cart> srcCartList, Long itemId, Integer num);

    /**
     * 从redis查找购物车
     * @param username
     * @return
     */

    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis里面
     * @param username
     * @param cartList
     */

    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车  将cookie中的购物车和redis中的购物车进行合并
     * @param cartList1
     * @param cartList2
     * @return
     */

    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
