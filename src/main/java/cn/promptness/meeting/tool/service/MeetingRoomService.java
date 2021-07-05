package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Room;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MeetingRoomService extends Service<List<Room>> {

    @Resource
    private HttpClientUtil httpClientUtil;

    @Override
    protected Task<List<Room>> createTask() {
        return new Task<List<Room>>() {
            @Override
            protected List<Room> call() throws Exception {
                HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/query_rooms.json", MeetingUtil.getHeaderList());
                Response<Room> response = httpResult.getContent(new TypeToken<Response<Room>>() {}.getType());
                if (MeetingUtil.checkCode(response.getCode())) {
                    return null;
                }
                return Optional.ofNullable(response.getResult()).orElse(new ArrayList<>());
            }
        };
    }

}
