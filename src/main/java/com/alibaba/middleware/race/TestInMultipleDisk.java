package com.alibaba.middleware.race;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by hahong on 2016/7/23.
 */
public class TestInMultipleDisk {
    /*
    static String orderFile = "D:\\middleware-data\\random\\random-order.txt";
    static String goodFile = "D:\\middleware-data\\random\\random-good.txt";
    static String buyerFile = "D:\\middleware-data\\random\\random-buyer.txt";
    */
    static String orderFile = "C:\\Users\\hahong\\Documents\\tmp\\random-order.txt";
    static String goodFile = "C:\\Users\\hahong\\Documents\\tmp\\random-good.txt";
    static String buyerFile = "C:\\Users\\hahong\\Documents\\tmp\\random-buyer.txt";
    static List<String> buyerIds = new ArrayList<>();
    static List<String> goodIds = new ArrayList<>();
    static long buyerCount = 100000;
    static long goodCount = 200000;
    static long orderCount = 10000000;

    public static void GenerateData() {



        for (int i = 0; i < buyerCount; ++i) {
            buyerIds.add(java.util.UUID.randomUUID().toString());
        }
        for (int i = 0; i < goodCount; ++i) {
            goodIds.add(java.util.UUID.randomUUID().toString());
        }
        try
        {
            //FileWriter out = new FileWriter(new File(buyerFile));

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(buyerFile), "UTF-8"), 1024*1024);
            for (String buyerId : buyerIds) {
                out.append(String.format("buyerid:%s\tb2:%s\tb3:%s\tb4:%s\tb5:%s\tb6:%s\tb7:%s\tb8:%s\n", buyerId, buyerId, buyerId, buyerId, buyerId, buyerId, buyerId, buyerId));
            }
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try
        {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(goodFile), "UTF-8"), 1024*1024);
            for (String goodId : goodIds) {
                out.append(String.format("goodid:%s\tb2:%s\tb3:%s\tb4:%s\tb5:%s\tb6:%s\tb7:%s\tb8:%s\n", goodId, goodId, goodId, goodId, goodId, goodId, goodId, goodId));
            }
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try
        {
            Random r = new Random();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(orderFile), "UTF-8"), 1024*1024);
            for (long i = 0; i < orderCount; ++i) {
                String rd = java.util.UUID.randomUUID().toString();
                String buyerId = buyerIds.get(r.nextInt((int) buyerCount));
                String goodId = goodIds.get(r.nextInt((int) goodCount));
                out.append(String.format("orderid:%s\tbuyerid:%s\tgoodid:%s\tcreatetime:%d\tb2:%s\tb3:%s\tb4:%s\tb5:%s\tb6:%s\tb7:%s\tb8:%s\n", i, buyerId, goodId, Math.abs(r.nextLong()), rd, rd, rd, rd, rd, rd, rd));
            }
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws IOException, InterruptedException {
        //GenerateData();
        System.out.println("Generate complete.");
        OrderSystemImpl impl = new OrderSystemImpl();
        long startTime = System.currentTimeMillis();
        List<String> orderFiles = Arrays.asList("C:\\Users\\hahong\\Documents\\tmp\\random-order1", "C:\\Users\\hahong\\Documents\\tmp\\random-order2", "D:\\middleware-data\\random\\random-order3", "D:\\middleware-data\\random\\random-order4");
        List<String> buyerFiles = Arrays.asList("C:\\Users\\hahong\\Documents\\tmp\\random-buyer1", "D:\\middleware-data\\random\\random-buyer2");
        List<String> goodFiles = Arrays.asList("C:\\Users\\hahong\\Documents\\tmp\\random-good1", "D:\\middleware-data\\random\\random-good2");
        List<String> storeFolders = Arrays.asList("C:\\Users\\hahong\\Documents\\tmp\\store\\", "D:\\middleware-data\\random\\store\\");
        impl.construct(orderFiles, buyerFiles, goodFiles, storeFolders);
        System.out.println("Construct complete.");
        System.out.printf("Time: %d.\n", (System.currentTimeMillis() - startTime) / 1000);
        Random rd = new Random();
        startTime = System.currentTimeMillis();
        for (int i = 0; ; ++i) {
            int type = rd.nextInt(3);
            if (type == 0) {
                impl.queryOrder(rd.nextInt((int) orderCount), null);
            } else if (type == 1) {
                impl.queryOrdersBySaler("", goodIds.get(rd.nextInt((int) goodCount)), null);
            } else {
                impl.queryOrdersByBuyer(0, Long.MAX_VALUE, buyerIds.get(rd.nextInt((int) buyerCount)));
            }
            System.out.printf("Query %d complete(QPS: %d).\n", i, i * 1000 / (System.currentTimeMillis() - startTime));
        }
    }
}
