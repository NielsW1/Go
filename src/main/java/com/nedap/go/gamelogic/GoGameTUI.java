package com.nedap.go.gamelogic;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoGameTUI {
    public static void main(String[] args) throws IOException {
        String input = "PASS";
        String[] parsed = input.split("~");
        System.out.println(parsed[0]);
    }
}
