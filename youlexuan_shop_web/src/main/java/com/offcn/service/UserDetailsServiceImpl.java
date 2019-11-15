package com.offcn.service;

import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {

        List<GrantedAuthority> list=new ArrayList<>();

        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        list.add(new SimpleGrantedAuthority("ROLE_USER"));

        //调用远程商家服务，根据商家账号获取商家信息
        TbSeller seller = sellerService.findOne(name);
        if(seller!=null) {
            //判断用户状态是否是审核通过
            if(seller.getStatus().equals("1")) {
                return new User(name, seller.getPassword(), list);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
}
