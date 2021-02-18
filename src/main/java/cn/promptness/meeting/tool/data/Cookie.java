package cn.promptness.meeting.tool.data;

import java.io.Serializable;

public class Cookie implements Serializable {

    private static final long serialVersionUID = 1890985422262564489L;

    protected String name;
    protected String value;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Cookie [name=" + name + ", value=" + value + "]";
    }
}
