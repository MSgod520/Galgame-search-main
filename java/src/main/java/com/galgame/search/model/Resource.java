package com.galgame.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Resource(
    String name,
    Object content, // Can be String or List<String>
    String password,
    Object type // Can be String or List<String>
) {
    public String getContentString() {
        if (content instanceof List list && !list.isEmpty()) {
            return String.valueOf(list.get(0));
        }
        return content != null ? content.toString() : "";
    }
}
