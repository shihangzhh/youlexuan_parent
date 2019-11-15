package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    public List<Cart> addGoodToCartList(List<Cart> srcCartList, Long itemId, Integer num) {




        //1、根据sku商品编号，获取商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //判断item sku商品对象是否为空
        if(item==null){
            throw new RuntimeException("添加到购物车商品:"+itemId+" 不存在");
        }
        //判断sku状态是否是审核通过
        if(!item.getStatus().equals("1")){
            //商品未通过审核
            throw  new RuntimeException("改商品审核未通过");
        }

        //2、获取商家id
        String sellerId = item.getSellerId();
        //3、根据商品id，从原有购物车集合，判断是否存在该商家购物车对象
        Cart cart = searchCartBySellerId(srcCartList, sellerId);
        //4、判断商家的购物车对象如果不存在
        if(cart==null){
            //创建购物车对象
            cart = new Cart();
            //设置购物车对象属性
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //购物明细 集合
            ArrayList<TbOrderItem> orderItems = new ArrayList<>();
            //创建具体购物明细
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItems.add(orderItem);
            cart.setOrderItemList(orderItems);

            //把新建的购物车对象，添加到原来购物车集合
            srcCartList.add(cart);
        }
        //5、如果该商家购物车对象存在
        else {
            //6、判断该商家购物车明细里面是否存在本次要添加到购物车商品
            TbOrderItem orderItem = searchOrderItemByItemid(cart.getOrderItemList(), itemId);

            if(orderItem==null){
                //该商品不存在购物车明细中，添加该商品到购物明细
                cart.getOrderItemList().add(createOrderItem(item,num));
            }else {
                //该商家购物车明细存在要添加到购物车的商品
                //修改商品购买数量、价格
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getTotalFee().add(new BigDecimal(item.getPrice().doubleValue()*num)));
                //修改完购买数量要判断数量是否大于0
                if(orderItem.getNum()<=0){
                    //移除该条购物明细
                    cart.getOrderItemList().remove(orderItem);
                }
                //判断该商家购物车如果购物明细小于等于等0，移除整个商家购物车
                if(cart.getOrderItemList().size()<=0){
                    srcCartList.remove(cart);
                }
            }

        }
        return srcCartList;
    }

    //购物车集合，判断是否存在该商家购物车对象

    public Cart searchCartBySellerId(List<Cart> srcList,String sellerId){
        //遍历原购物车集合
        if(srcList!=null) {
            for (Cart cart : srcList) {
                //判断购物车对象里面商家编号，是否等于传递过来的商家id
                if (cart.getSellerId().equals(sellerId)) {
                    return cart;
                }
            }
        }

        return null;
    }

    //创建购物明细
    public TbOrderItem createOrderItem(TbItem item,Integer num){

        if(num<=0){
            throw new RuntimeException("购买数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        orderItem.setPicPath(item.getImage());

        return orderItem;
    }

    //判断指定购物车明细，是否存在指定的商品
    public TbOrderItem searchOrderItemByItemid(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return  orderItem;
            }
        }

        return  null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {

        System.out.println("从redis提取出来的用户名是"+username);

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if (cartList == null){
            cartList = new ArrayList<>();

        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {

        System.out.println("向redis中存入数据"+username);

        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        for (Cart cart : cartList2) {

            for (TbOrderItem orderItem : cart.getOrderItemList()) {

             cartList1 = addGoodToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());

            }
        }
        return cartList1;
    }
}
