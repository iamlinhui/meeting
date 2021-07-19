package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.service.ValidateUserService;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import javafx.application.Platform;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 检查登录状态
 *
 * @author lynn
 * @date 2021/1/8 14:18
 * @since v1.0.0
 */
@Component
public class ContinuationTask {

    @Resource
    private ConfigurableApplicationContext applicationContext;

    @Scheduled(initialDelay = 10000, fixedRate = 15000)
    public void continuation() {
        if (MeetingUtil.haveAccount()) {
            Platform.runLater(() -> applicationContext.getBean(ValidateUserService.class).expect(null).start());
        }
    }
}
