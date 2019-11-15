package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {

        System.out.println("接收到到入solr数据请求");
 //对消息的种类进行判断
        if (message instanceof TextMessage){

            try {
                TextMessage textMessage = (TextMessage)message;
                String jsonStr = textMessage.getText();

                List<TbItem> itemList = JSON.parseArray(jsonStr, TbItem.class);

                for (TbItem item : itemList) {
                    System.out.println("商品标题是"+item.getTitle());

                    //获取sku对应的商品sku
                    Map<String,Object> specMap = JSON.parseObject(item.getSpec(),Map.class);

                    //创建一个新的map存储对sku的转化
                    Map  pinyinMap = new HashMap();
                    Set<String> itemSet = specMap.keySet();
                    for (String key : itemSet) {

                   pinyinMap.put("item_spec_"+Pinyin.toPinyin(key.toString(),"").toLowerCase(),specMap.get(key));

                    }
                         //关联动态域
                    item.setSpecMap(pinyinMap);

                }

                solrTemplate.saveBeans(itemList);
                solrTemplate.commit();


            } catch (JMSException e) {
                e.printStackTrace();
            }


        }


    }
}
