package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class ItemPageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {


        if (message instanceof ObjectMessage){

            try {
                ObjectMessage objectMessage = (ObjectMessage) message;

                Long [] ids = (Long []) objectMessage.getObject();

                itemPageService.deleteHtml(ids);
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }
}
