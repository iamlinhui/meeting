package cn.promptness.meeting.tool.config;

import cn.promptness.httpclient.HttpClientProperties;
import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.meeting.tool.data.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClientProperties httpClientProperties() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setAgent(Constant.USER_AGENT);
        return properties;
    }

    @Bean
    public HttpClientUtil httpClientUtil(HttpClientProperties properties) {
        return new HttpClientUtil(properties);
    }
}
