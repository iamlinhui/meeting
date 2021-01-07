package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.utils.OpenUtil;
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

import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class CheckLoginService extends Service<Boolean> {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    private String token;
    private String time;
    private Stage alert;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                while (alert.isShowing()) {
                    Thread.sleep(3000);
                    URIBuilder builder = new URIBuilder(String.format("https://passport.oa.fenqile.com/user/main/scan.json?token=%s&_=%s", token, time));
                    HttpGet httpGet = new HttpGet();
                    httpGet.setHeader(OpenUtil.getHeader());
                    httpGet.setURI(builder.build());
                    try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(USER_AGENT).build().execute(httpGet)) {
                        String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                        JSONObject jsonObject = new JSONObject(content);
                        int loginSuccess = jsonObject.getInt("login_success");
                        if (loginSuccess == 1) {
                            Header[] headers = closeableHttpResponse.getHeaders("Set-Cookie");
                            OpenUtil.flashHeader(headers);
                            return Boolean.TRUE;
                        }
                    }
                }
                return Boolean.FALSE;
            }
        };
    }


    public String getToken() {
        return token;
    }

    public CheckLoginService setToken(String token) {
        this.token = token;
        return this;
    }

    public String getTime() {
        return time;
    }

    public CheckLoginService setTime(String time) {
        this.time = time;
        return this;
    }

    public Stage getAlert() {
        return alert;
    }

    public CheckLoginService setAlert(final Stage alert) {
        this.alert = alert;
        return this;
    }
}
