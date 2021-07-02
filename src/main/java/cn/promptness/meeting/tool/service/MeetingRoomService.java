package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MeetingRoomService extends Service<JsonArray> {

    @Resource
    private HttpClientUtil httpClientUtil;

    @Override
    protected Task<JsonArray> createTask() {
        return new Task<JsonArray>() {
            @Override
            protected JsonArray call() throws Exception {
                HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/query_rooms.json", MeetingUtil.getHeaderList());
                JsonObject jsonObject = httpResult.getContent(JsonObject.class);
                int code = jsonObject.get("retcode").getAsInt();
                boolean open = MeetingUtil.checkCode(code);
                if (open) {
                    return null;
                }
                return jsonObject.has("result_rows") ? jsonObject.getAsJsonArray("result_rows") : new JsonArray();
            }
        };
    }

}
