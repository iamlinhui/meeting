package cn.promptness.meeting.tool.task;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MeetingTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MeetingTask.class);

    public MeetingTask(MeetingTaskProperties meetingTaskProperties, HttpClientUtil httpClientUtil) {
        this.meetingTaskProperties = meetingTaskProperties;
        this.httpClientUtil = httpClientUtil;
    }

    private final MeetingTaskProperties meetingTaskProperties;
    private final HttpClientUtil httpClientUtil;

    public Boolean meeting() throws Exception {
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.isEnable())) {
            return false;
        }

        Map<String, String> paramMap = this.getParamMap();

        List<String> roomIdList = meetingTaskProperties.getRoomIdList();
        log.info("---开始发送请求---");
        while (true) {
            final String end = "---结束发送请求---";
            // 0默认 1成功  2时间未到  3已经被占有了
            int[] result = new int[roomIdList.size()];
            for (int i = 0; i < roomIdList.size(); i++) {
                String roomId = roomIdList.get(i);
                paramMap.put("room_id", roomId);
                log.info("---预定{}会议室---", Constant.ROOM_INFO_LIST.get(roomId));
                HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/due_meeting.json", paramMap, MeetingUtil.getHeaderList());
                result[i] = this.checkContent(httpResult);
                if (result[i] == 1 && !Objects.equals(Boolean.TRUE, meetingTaskProperties.getMultipleChoice())) {
                    log.info(end);
                    return true;
                }
            }

            List<Integer> resultList = Arrays.stream(result).boxed().collect(Collectors.toList());

            // 非多选
            if (!Objects.equals(Boolean.TRUE, meetingTaskProperties.getMultipleChoice())) {
                if (Arrays.stream(result).sum() == roomIdList.size() * 3) {
                    log.info(end);
                    return false;
                }
            }
            // 多选
            else {
                if (!resultList.contains(2)) {
                    log.info(end);
                    return false;
                }
            }
        }
    }

    private int checkContent(HttpResult httpResult) {
        if (httpResult.isSuccess()) {
            MeetingUtil.addHeader(httpResult.getHeaderList("Set-Cookie"));
        }
        // 0默认 1成功  2时间未到  3已经被占有了
        Response<?> response = httpResult.getContent(Response.class);
        if (response.getCode() == 0) {
            return 1;
        }
        String message = response.getMessage();
        log.error(message);
        final String conflict = "预定的会议室冲突";
        if (message.contains(conflict)) {
            return 3;
        }
        final String future = "只能预定未来7天内的会议室";
        if (message.contains(future)) {
            return 2;
        }
        return 3;
    }

    private Map<String, String> getParamMap() {
        Map<String, String> paramMap = new HashMap<>(16);
        paramMap.put("meeting_type_id", "1");
        paramMap.put("meeting_name", "工作汇报");
        paramMap.put("city", "深圳市");
        paramMap.put("address", "中国储能大厦");
        paramMap.put("meeting_date", LocalDate.now().plusDays(meetingTaskProperties.getPlusDays()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        paramMap.put("start_time", meetingTaskProperties.getStartTime());
        paramMap.put("end_time", meetingTaskProperties.getEndTime());
        paramMap.put("meeting_person", MeetingUtil.getUid());
        return paramMap;
    }

    @Override
    public void run() {
        if (MeetingUtil.haveAccount()) {
            try {
                meeting();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
