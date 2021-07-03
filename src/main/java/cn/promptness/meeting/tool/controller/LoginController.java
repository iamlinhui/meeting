package cn.promptness.meeting.tool.controller;

import cn.promptness.httpclient.HttpClientUtil;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Controller
public class LoginController {
    @Resource
    private HttpClientUtil httpClientUtil;

    private String currentTimeMillis;
    private String token;
    private boolean codeSuccess;

    @FXML
    public ImageView codeImageView;

    public void initialize() {
        currentTimeMillis = String.valueOf(System.currentTimeMillis());
        token = DigestUtils.md5Hex(currentTimeMillis);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            httpClientUtil.doGet("https://passport.oa.fenqile.com/user/main/qrcode.png?token=" + getToken(), byteArrayOutputStream);
            codeImageView.setImage(new Image(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
            codeSuccess = true;
        } catch (Exception e) {
            codeSuccess = false;
        }
    }

    public String getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public String getToken() {
        return token;
    }

    public boolean isCodeSuccess() {
        return codeSuccess;
    }
}
