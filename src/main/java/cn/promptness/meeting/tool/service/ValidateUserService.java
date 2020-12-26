package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.utils.OpenUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class ValidateUserService extends Service<Void> {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                URIBuilder builder = new URIBuilder("https://api.oa.fenqile.com/oa/api/user/session.json");
                HttpGet httpGet = new HttpGet();
                httpGet.setHeader(OpenUtil.getHeader());
                httpGet.setURI(builder.build());

                try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(USER_AGENT).build().execute(httpGet)) {
                    String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                    JSONObject jsonObject = new JSONObject(content);
                    int code = jsonObject.getInt("retcode");
                    OpenUtil.open(code);
                }
                return null;
            }
        };
    }

}
