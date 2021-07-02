package cn.promptness.meeting.tool.config;

import cn.promptness.httpclient.HttpClientProperties;
import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.meeting.tool.data.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClientUtil httpClientUtil() {
        HttpClientProperties httpClientProperties = new HttpClientProperties();
        httpClientProperties.setAgent(Constant.USER_AGENT);
        return new HttpClientUtil(httpClientProperties);
    }
}
