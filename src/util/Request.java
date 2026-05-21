package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;
    private Map<String, Object> data;

    public Request(String action) {
        this.action = action;
        this.data = new HashMap<>();
    }

    public String getAction() {
        return action;
    }

    public Request put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data.get(key);
    }
}