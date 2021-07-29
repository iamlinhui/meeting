package cn.promptness.meeting.tool.exception;

public class MeetingException extends RuntimeException{

    public MeetingException() {
    }

    public MeetingException(String message) {
        super(message);
    }
}
