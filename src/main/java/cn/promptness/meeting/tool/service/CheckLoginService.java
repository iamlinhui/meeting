package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.controller.LoginController;
import cn.promptness.meeting.tool.pojo.Login;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.http.Header;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Scope("prototype")
public class CheckLoginService extends Service<Boolean> {
    @Resource
    private LoginController loginController;
    @Resource
    private HttpClientUtil httpClientUtil;

    private Stage loginStage;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                while (loginStage.isShowing() && loginController.isCodeSuccess()) {
                    Thread.sleep(3000);
                    HttpResult httpResult = httpClientUtil.doGet(String.format("https://passport.oa.fenqile.com/user/main/scan.json?token=%s&_=%s", loginController.getToken(), loginController.getCurrentTimeMillis()), MeetingUtil.getHeaderList());
                    Login login = httpResult.getContent(Login.class);
                    if (login.isSuccess()) {
                        List<Header> headerList = httpResult.getHeaderList("Set-Cookie");
                        MeetingUtil.flashHeader(headerList);
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            }
        };
    }

    public CheckLoginService setStage(final Stage loginStage) {
        this.loginStage = loginStage;
        return this;
    }
}
