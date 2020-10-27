package com.fcfutre.example.masterworker.worker;

public class WorkerInfo {

    String ip;
    String cpuUsage;
    String memoryUsage;

    public WorkerInfo(String ip, String cpuUsage, String memoryUsage) {
        this.ip = ip;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    @Override
    public String toString() {
        return "WorkerInfo{" +
                "ip='" + ip + '\'' +
                ", cpuUsage='" + cpuUsage + '\'' +
                ", memoryUsage='" + memoryUsage + '\'' +
                '}';
    }
}
