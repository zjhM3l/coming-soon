package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSONObject;

@SpringBootTest
public class HttpClientTest {

    /**
     * 测试通过httpclient发送GET方式请求
     */
    @Test
    public void testGET() throws Exception {
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建请求对象
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");
        // 发送请求，接收响应
        CloseableHttpResponse response = httpClient.execute(httpGet);
        // 获取服务端返回的状态码
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println(statusCode);
        // 获得响应体
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        System.err.println("服务端返回的数据为" + body);
        // 释放资源
        response.close();
        httpClient.close();
    }

    /**
     * 测试通过httpclient发送POST方式请求
     */
    @Test
    public void testPOST() throws Exception {
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建请求对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        
        // 设置请求参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "admin");
        jsonObject.put("password", "123456");
        StringEntity entity = new StringEntity(jsonObject.toString());
        // 指定请求编码方式
        entity.setContentEncoding("utf-8");
        // 指定数据格式
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        
        // 发送请求，接收响应
        CloseableHttpResponse response = httpClient.execute(httpPost);
        // 解析响应结果
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println(statusCode);
        HttpEntity responseEntity = response.getEntity();
        String body = EntityUtils.toString(responseEntity);
        System.err.println("服务端返回的数据为" + body);

        // 释放资源
        response.close();
        httpClient.close();
    }

}
