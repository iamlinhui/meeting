package cn.promptness.meeting.tool.config;

import cn.promptness.httpclient.HttpClientProperties;
import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.meeting.tool.data.Constant;
import org.apache.http.client.config.CookieSpecs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClientUtil httpClientUtil() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setAgent(Constant.USER_AGENT);
        properties.setCookieSpecs(CookieSpecs.IGNORE_COOKIES);
        return new HttpClientUtil(properties);
    }
}
