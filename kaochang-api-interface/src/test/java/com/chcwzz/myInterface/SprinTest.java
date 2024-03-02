package com.chcwzz.myInterface;

import com.chcwzzz.myInterface.KaoChangInterfaceApplication;
import com.chcwzzz.myInterface.domain.User;
import com.chcwzzz.sdk.client.KaochangClient;
import com.chcwzzz.sdk.model.DevRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KaoChangInterfaceApplication.class)
public class SprinTest {

    @Autowired
    private KaochangClient kaochangClient;

    @Test
    public void testStarter() {
        User userName = new User();
        userName.setUserName("测试请求");
        DevRequest devRequest = new DevRequest();
        devRequest.setUrl("http://localhost:8123/api/name/user");
        devRequest.setBody(userName);
        String response = kaochangClient.doPost(devRequest);
        System.out.println("response = " + response);
    }
}
