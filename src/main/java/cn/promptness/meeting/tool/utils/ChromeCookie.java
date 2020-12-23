package cn.promptness.meeting.tool.utils;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@EqualsAndHashCode
@Getter
@Setter
@Builder
public class ChromeCookie {
    protected String name;
    protected String value;
    protected Date expires;
    protected String path;
    protected String domain;
    protected boolean secure;
    protected boolean httpOnly;

    @Override
    public String toString() {
        return "Cookie [name=" + name + ", value=" + value + "]";
    }
}
