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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class MeetingRoomService extends Service<JSONArray> {

    @Override
    protected Task<JSONArray> createTask() {
        return new Task<JSONArray>() {
            @Override
            protected JSONArray call() throws Exception {

                URIBuilder builder = new URIBuilder("https://m.oa.fenqile.com/meeting/main/query_rooms.json");
                HttpGet httpGet = new HttpGet();
                httpGet.setHeader(MeetingUtil.getHeader());
                httpGet.setURI(builder.build());

                try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
                    String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                    JSONObject jsonObject = new JSONObject(content);
                    int code = jsonObject.getInt("retcode");
                    boolean open = MeetingUtil.checkCode(code);
                    if (open) {
                        return null;
                    }
                    return jsonObject.has("result_rows") ? jsonObject.getJSONArray("result_rows") : new JSONArray();
                }
            }
        };
    }

}
