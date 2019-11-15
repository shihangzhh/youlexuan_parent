package com.offcn.search.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class ItemSearchDeleteListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {

        if (message instanceof ObjectMessage){

            try {
                ObjectMessage objectMessage = (ObjectMessage)message;
                Long [] ids = (Long[]) objectMessage.getObject();

                for (Long id : ids) {
                    System.out.println("删除搜索引擎索引数据"+id);
                    solrTemplate.deleteById(id+"");
                    solrTemplate.commit();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
        
    }
}
