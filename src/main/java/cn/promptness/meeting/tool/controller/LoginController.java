package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MdUtil;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {


    private String currentTimeMillis;
    private String token;

    @FXML
    public ImageView codeImageView;

    public void initialize() throws Exception {
        currentTimeMillis = String.valueOf(System.currentTimeMillis());
        token = MdUtil.encipher(currentTimeMillis);

        URIBuilder builder = new URIBuilder("https://passport.oa.fenqile.com/user/main/qrcode.png?token=" + getToken());
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(builder.build());

        try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
            codeImageView.setImage(new Image(closeableHttpResponse.getEntity().getContent()));
        }
    }

    public String getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public String getToken() {
        return token;
    }

}
