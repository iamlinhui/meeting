package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import com.google.gson.JsonObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ValidateUserService extends Service<String> {

    @Resource
    private HttpClientUtil httpClientUtil;

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                if (!MeetingUtil.haveAccount()) {
                    return "";
                }
                HttpResult httpResult = httpClientUtil.doGet("https://api.oa.fenqile.com/oa/api/user/session.json", MeetingUtil.getHeaderList());
                JsonObject jsonObject = httpResult.getContent(JsonObject.class);
                int code = jsonObject.get("retcode").getAsInt();
                if (code == 0) {
                    return jsonObject.getAsJsonArray("result_rows").get(0).getAsJsonObject().get("name").getAsString();
                }
                return "";
            }
        };
    }

}
