package com.offcn;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importSolr(){
        //1、读取sku表数据中 状态为 审核通过
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");

        List<TbItem> list = itemMapper.selectByExample(example);
        for (TbItem item : list) {
            System.out.println("标题:"+item.getTitle());

            Map mapSpec = JSON.parseObject(item.getSpec(), Map.class);

            //定义一个map存储key为拼音的规格
            Map<String,String> mapPinYin=new HashMap<>();
            for (Object key : mapSpec.keySet()) {
                mapPinYin.put(Pinyin.toPinyin(key.toString(),"").toLowerCase(),(String) mapSpec.get(key))  ;
            }
            //设置到动态域字段
            item.setSpecMap(mapPinYin);
            //待处理数据导入
        }

        //保存list集合到solr
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        //手动加载spring配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil=(SolrUtil) context.getBean("solrUtil");

        solrUtil.importSolr();
    }

}
