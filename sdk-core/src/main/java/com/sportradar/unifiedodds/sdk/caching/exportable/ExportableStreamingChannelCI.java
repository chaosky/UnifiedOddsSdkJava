package com.sportradar.unifiedodds.sdk.caching.exportable;

import java.io.Serializable;

@SuppressWarnings({ "AbbreviationAsWordInName", "HiddenField" })
public class ExportableStreamingChannelCI implements Serializable {

    private int id;
    private String name;

    public ExportableStreamingChannelCI(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
