package com.spacex.lombok.test;

import java.util.List;

@Getter
public class App {
    private String value;
    private String value2;

    public App(String value) {
        this.value = value;
    }

    public static void main(String[] args) {
        App app = new App("it works");
        //System.out.println(app.getValue());
    }
}
