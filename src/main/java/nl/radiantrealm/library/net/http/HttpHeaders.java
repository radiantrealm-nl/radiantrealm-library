package nl.radiantrealm.library.net.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHeaders {
    private final Map<String, List<String>> map = new HashMap<>();

    public HttpHeaders() {}

    public HttpHeaders(Map<String, List<String>> map) {
        this.map.putAll(map);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String string : entry.getValue()) {
                builder.append(String.format(
                        "%s: %s\r\n",
                        entry.getKey(),
                        string
                ));
            }
        }

        return builder.toString();
    }

    public void add(String key, String value) {
        map.computeIfAbsent(key.toLowerCase(), k -> new ArrayList<>()).add(value);
    }

    public List<String> get(String key) {
        return map.get(key.toLowerCase());
    }

    public String getFirst(String key) {
        List<String> list = map.get(key.toLowerCase());

        if (list == null) {
            return null;
        }

        return list.getFirst();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key.toLowerCase());
    }

    public void remove(String key) {
        map.remove(key.toLowerCase());
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Map<String, List<String>> asMap() {
        return new HashMap<>(map);
    }
}
