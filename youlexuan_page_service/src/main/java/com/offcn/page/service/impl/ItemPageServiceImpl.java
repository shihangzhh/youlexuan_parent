package com.offcn.page.service.impl;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import  freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pageDir}")
    private  String pageDir;

    @Autowired
private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;



    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {

        Configuration configuration = freeMarkerConfig.getConfiguration();
        //获取模板文件
        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据模型对象
            Map map=new HashMap();
            //读取商品基本信息
            System.out.println("商品的id是"+goodsId);
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);

            System.out.println("商品是"+goods);
            map.put("goods",goods);
            //读取商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",goodsDesc);
            System.out.println("商品的扩展信息是"+goodsDesc);

            //读取商品的分类信息  用来做面包屑导航栏
            //获取商品以及分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("itemCat1",itemCat1);
            map.put("itemCat2",itemCat2);
            map.put("itemCat3",itemCat3);

            //4.SKU 列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//状态为有效
            criteria.andGoodsIdEqualTo(goodsId);//指定 SPU ID
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默 认

            List<TbItem> itemList = itemMapper.selectByExample(example);

            map.put("itemList",itemList);

            //创建静态页面输出对象
            FileWriter out = new FileWriter(new File(pageDir + goodsId + ".html"));

            //执行模板渲染
            template.process(map,out);

            out.close();

            return  true;


        } catch (IOException e) {
            e.printStackTrace();
        }catch (TemplateException e){
            e.printStackTrace();
        }


        return false;
    }

    @Override
    public void deleteHtml(Long[] ids) {

        for (Long id : ids) {
            boolean flag = new File(pageDir + id + ".html").delete();
            System.out.println("删除静态页面结果"+id+flag);
        }


    }
}
