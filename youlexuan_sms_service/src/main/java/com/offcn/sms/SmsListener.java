package com.offcn.sms;

import com.aliyuncs.CommonResponse;
import com.offcn.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component("smsListener")
public class SmsListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {

        if (message instanceof MapMessage){

            try {
                MapMessage mapMessage = (MapMessage) message;
                String mobile = mapMessage.getString("mobile");
                String template_code = mapMessage.getString("template_code");
                String sign_name = mapMessage.getString("sign_name");
                String code = mapMessage.getString("code");
                CommonResponse response = smsUtil.sendSms(mobile, template_code, sign_name, code);

                System.out.println("响应的数据data"+response.getData());

            } catch (JMSException e) {


            }catch (Exception e){
                e.printStackTrace();
            }






        }

    }
}
