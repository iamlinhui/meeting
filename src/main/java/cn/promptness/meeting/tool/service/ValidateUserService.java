package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.controller.MenuController;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Session;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ValidateUserService extends BaseService<String> {

    @Resource
    private HttpClientUtil httpClientUtil;
    @Resource
    private MenuController menuController;

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                if (!AccountCache.haveAccount()) {
                    return null;
                }
                HttpResult httpResult = httpClientUtil.doGet("https://api.oa.fenqile.com/oa/api/user/session.json?resource_sn=NEWAPR_OA", AccountCache.getHeaderList());
                Response<Session> response = httpResult.getContent(new TypeToken<Response<Session>>() {}.getType());
                if (response.isSuccess()) {
                    return response.getResult().stream().findFirst().orElse(new Session()).getName();
                }
                return null;
            }
        };
    }

    @Override
    public Service<String> expect(Callback callback) {
        super.setOnSucceeded(event -> {
            if (StringUtils.isEmpty(event.getSource().getValue())) {
                Stage stage = SystemTrayUtil.getPrimaryStage();
                if (stage.isIconified()) {
                    stage.setIconified(false);
                }
                if (!stage.isShowing()) {
                    stage.show();
                }
                stage.toFront();
                menuController.login();
                return;
            }
            if (callback != null) {
                callback.call(event);
            }
        });
        return this;
    }

}
