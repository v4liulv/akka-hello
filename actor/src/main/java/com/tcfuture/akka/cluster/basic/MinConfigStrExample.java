package com.tcfuture.akka.cluster.basic;

/**
 * @author liulv
 * @since 1.0.0
 */
public class MinConfigStrExample {

    public static void main(String[] args) {
        String minConfig = "akka { \n"
                + "  actor.provider = cluster \n"
                + "  remote.artery { \n"
                + "    canonical { \n"
                + "      hostname = \"127.0.0.1\" \n"
                + "      port = 2551 \n"
                + "    } \n"
                + "  } \n"
                + "}  \n";

        System.out.println(minConfig);
    }
}
