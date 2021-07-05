package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Session;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import com.google.gson.reflect.TypeToken;
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
                    return null;
                }
                HttpResult httpResult = httpClientUtil.doGet("https://api.oa.fenqile.com/oa/api/user/session.json", MeetingUtil.getHeaderList());
                Response<Session> response = httpResult.getContent(new TypeToken<Response<Session>>() {}.getType());
                if (response.isSuccess()) {
                    return response.getResult().stream().findFirst().orElse(new Session()).getName();
                }
                return null;
            }
        };
    }

}
