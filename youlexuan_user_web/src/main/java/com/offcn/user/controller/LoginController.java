package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/login")
public class LoginController {

@RequestMapping(value = "/showName")
    public Map showName(){


        String name = SecurityContextHolder.getContext().getAuthentication().getName();//得到登录人的账号

        Map map = new HashMap();
        map.put("loginName",name);

        return  map;
    }

}
