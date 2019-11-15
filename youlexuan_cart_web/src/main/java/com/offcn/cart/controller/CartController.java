package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.pojo.TbAddress;
import com.offcn.user.service.AddressService;
import com.offcn.util.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Reference(timeout = 6000)
private CartService cartService;

    @Reference
    private AddressService addressService;


    /**
     * 购物车列表
     * @return
     */
    @RequestMapping(value = "/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){

        //获取登录系统的用户名 如果没有登录的话，用户名为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户名为"+username);

        //从cookie中获取购物车
        String cartList = CookieUtil.getCookieValue(request,"cartList","UTF-8");

        if (cartList == null || cartList.equals("")){
            cartList="[]";
        }

        List<Cart> cartList_Cookie = null;
        cartList_Cookie = JSON.parseArray(cartList,Cart.class);
        if (username.equals("anonymousUser")){//未登录的状态。从cookie中获取

            if (cartList_Cookie!= null){
                return cartList_Cookie;
            }else {

                return  new ArrayList<>();
            }

        }else {

            //从redis中获取
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);

            if (cartList_Cookie.size()>0){ //如果本地存有购物车数据
                //合并购物车
                cartListFromRedis = cartService.mergeCartList(cartListFromRedis, cartList_Cookie);

                //清除本地cookie数据
                CookieUtil.deleteCookie(request,response,"cartList");

                //将合并后的数据保存到redis
                cartService.saveCartListToRedis(username,cartListFromRedis);
            }
            return cartListFromRedis;

        }


    }

    //cart/addGoodsToCartList.do   跨域注解
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    @RequestMapping(value = "/addGoodsToCartList")
   public Result addGoodsToCartList(HttpServletRequest request,HttpServletResponse response, Long itemId,Integer num){

        //获取登录系统的用户名 如果没有登录的话，用户名为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户名为"+username);
        try {
            List<Cart> cartList = findCartList(request,response);

            List<Cart> cartList1 = cartService.addGoodToCartList(cartList, itemId, num);
            System.out.println("cartList+++"+cartList1);

            if (username.equals("anonymousUser")){ //保存到cookie

                CookieUtil.setCookie(request,response,"cartList", JSON.toJSONString(cartList1),3600*24,"UTF-8");
                System.out.println("向cookie中保存数据");
            }else{  //保存到redis

                System.out.println("数据保存到了redis中");
                cartService.saveCartListToRedis(username,cartList1);

            }


            return   new Result(true,"添加商品成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"添加商品失败");
        }


   }
//cart/findListByUserId.do
@RequestMapping(value = "/findListByUserId")
   public  List<TbAddress> findListByUserId(){

    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

    return  addressService.findListByUserId(userId);
     }





}
