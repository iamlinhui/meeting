package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.controller.LoginController;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class CheckLoginService extends Service<Boolean> {


    @Resource
    private LoginController loginController;
    private Stage loginStage;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                while (loginStage.isShowing()) {
                    Thread.sleep(3000);
                    URIBuilder builder = new URIBuilder(String.format("https://passport.oa.fenqile.com/user/main/scan.json?token=%s&_=%s", loginController.getToken(), loginController.getCurrentTimeMillis()));
                    HttpGet httpGet = new HttpGet();
                    httpGet.setHeader(MeetingUtil.getHeader());
                    httpGet.setURI(builder.build());
                    try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
                        String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                        JSONObject jsonObject = new JSONObject(content);
                        int loginSuccess = jsonObject.getInt("login_success");
                        if (loginSuccess == 1) {
                            Header[] headers = closeableHttpResponse.getHeaders("Set-Cookie");
                            MeetingUtil.flashHeader(headers);
                            return Boolean.TRUE;
                        }
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
