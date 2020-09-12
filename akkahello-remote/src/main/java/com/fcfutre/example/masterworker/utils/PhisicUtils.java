package com.fcfutre.example.masterworker.utils;

import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PhisicUtils {

    private static String localIP = null;
    @Getter
    private static String cpuUsage = null;
    @Getter
    private static String memoryUsage = null;

    public static String getLocalIP() {
        try {
            localIP = InetAddress.getLocalHost().toString().split("/")[1];
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return localIP;
    }
}
