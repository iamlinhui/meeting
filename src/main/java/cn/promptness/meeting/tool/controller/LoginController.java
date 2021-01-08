package cn.promptness.meeting.tool.controller;

import cn.promptness.meeting.tool.utils.MdUtil;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {


    private String currentTimeMillis;
    private String token;

    @FXML
    public ImageView codeImageView;

    public void initialize() {
        currentTimeMillis = String.valueOf(System.currentTimeMillis());
        token = MdUtil.encipher(currentTimeMillis);
        codeImageView.setImage(new Image("https://passport.oa.fenqile.com/user/main/qrcode.png?token=" + getToken()));
    }

    public String getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public String getToken() {
        return token;
    }

}
