package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.controller.MenuController;
import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 检查登录状态
 *
 * @author lynn
 * @date 2021/1/8 14:18
 * @since v1.0.0
 */
public class ContinuationTask {

    @Resource
    private MenuController menuController;
    @Resource
    private ConfigurableApplicationContext applicationContext;

    @Scheduled(initialDelay = 10000, fixedRate = 15000)
    public void continuation() {
        if (MeetingUtil.haveAccount()) {
            Platform.runLater(() -> {
                ValidateUserService validateUserService = applicationContext.getBean(ValidateUserService.class);
                validateUserService.start();
                validateUserService.setOnSucceeded(event -> {
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
                    }
                });
            });
        }
    }
}
