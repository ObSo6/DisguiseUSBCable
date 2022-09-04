package com.obso6.disguiseapp.config;

public class ScriptTable {

    //脚本的id，名字，内容，副标题
    private String id;
    private String name;
    private String content;
    private String introduce;

    public ScriptTable() {

    }

    public ScriptTable(String id, String name, String content, String introduce) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.introduce = introduce;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getIntroduce() {
        return introduce;
    }

}
