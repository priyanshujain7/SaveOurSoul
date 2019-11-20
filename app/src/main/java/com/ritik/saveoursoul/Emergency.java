package com.ritik.saveoursoul;

public class Emergency {
    private String id;
    private String number;
    private String name;

    public Emergency(){

    }

    public Emergency(String id, String number, String name) {
        this.id = id;
        this.number = number;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

}
