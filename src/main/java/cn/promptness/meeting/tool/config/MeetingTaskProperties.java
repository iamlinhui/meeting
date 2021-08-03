package cn.promptness.meeting.tool.config;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MeetingTaskProperties implements Serializable {

    private static final long serialVersionUID = 7209286665289125724L;

    private final LocalDate meetingDate;
    private final String startTime;
    private final String endTime;
    private final List<String> roomIdList;
    private final Integer target;

    public MeetingTaskProperties(LocalDate meetingDate, String startTime, String endTime, List<String> roomIdList, Integer target) {
        this.meetingDate = meetingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomIdList = roomIdList;
        this.target = target;
    }

    @Override
    public String toString() {
        if (meetingDate == null || startTime == null || endTime == null || CollectionUtils.isEmpty(roomIdList)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String meetingDateString = getMeetingDateString();
        stringBuilder.append("会议时间:\n").append(meetingDateString).append("(").append(MeetingUtil.dateToWeek(meetingDateString)).append(")").append(startTime).append("-").append(endTime).append("\n\n");
        stringBuilder.append("预定列表:\n");
        for (String roomId : roomIdList) {
            stringBuilder.append(Constant.ROOM_INFO_MAP.get(roomId)).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public List<String> getRoomIdList() {
        return roomIdList;
    }

    public Integer getTarget() {
        return target;
    }

    public boolean isEnable() {
        LocalDate now = LocalDate.now();
        LocalDate handleDate = meetingDate.plusDays(-7);
        // 能预定的日期
        return !now.isBefore(handleDate);
    }

    public boolean checkTimeIsOk() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime localTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime meetingStart = meetingDate.atTime(localTime);
        return now.isBefore(meetingStart);
    }

    public String getMeetingDateString() {
        return meetingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }
}
