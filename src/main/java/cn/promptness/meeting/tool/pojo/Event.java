package cn.promptness.meeting.tool.pojo;

public class Event {

    private final Integer target;
    private final Boolean result;

    public Event(Integer target, Boolean result) {
        this.target = target;
        this.result = result;
    }

    public Integer getTarget() {
        return target;
    }

    public Boolean getResult() {
        return result;
    }
}
