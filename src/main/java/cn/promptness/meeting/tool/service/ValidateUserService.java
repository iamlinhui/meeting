package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
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
public class ValidateUserService extends Service<String> {

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {

                URIBuilder builder = new URIBuilder("https://api.oa.fenqile.com/oa/api/user/session.json");
                HttpGet httpGet = new HttpGet();
                httpGet.setHeader(MeetingUtil.getHeader());
                httpGet.setURI(builder.build());

                try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
                    String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                    JSONObject jsonObject = new JSONObject(content);
                    int code = jsonObject.getInt("retcode");
                    if (code == 0) {
                        return jsonObject.getJSONArray("result_rows").getJSONObject(0).getString("name");
                    }
                }
                return "";
            }
        };
    }

}
