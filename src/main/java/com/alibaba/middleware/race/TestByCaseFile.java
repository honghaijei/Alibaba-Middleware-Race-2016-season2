package com.alibaba.middleware.race;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hahong on 2016/7/19.
 */
public class TestByCaseFile {
    public static String[] getKeys(String str) {
        List<String> r = new ArrayList<>();
        String[] keys = str.replace("KEYS:[", "").replace("]", "").split(",");
        for (int i = 0; i < keys.length; ++i) {
            String s = keys[i].trim();
            if (!s.equals("")) {
                r.add(s);
            }
        }
        return r.toArray(new String[r.size()]);
    }
    public static void checkResult(String str, OrderSystem.Result r) {
        Pattern pattern = Pattern.compile("orderid:(\\d+)");
        Matcher matcher = pattern.matcher(str);
        matcher.find();
        //System.out.println(str);
        long orderId = Long.parseLong(matcher.group(1));
        if (r.orderId() != orderId) {
            System.err.printf("Error, key: %s, expected value: %d, actural value: %d\n", "orderid", orderId, r.orderId());
        }
        pattern = Pattern.compile("KV:\\[(.*)\\]");
        matcher = pattern.matcher(str);
        matcher.find();
        String kvStr = matcher.group(1);
        String[] kvs = kvStr.split(",");
        try {
            for (String kv : kvs) {
                String t = kv.trim();

                if (t.equals("")) continue;
                String[] tp = t.split(":");
                String k = tp[0], v = tp[1];
                if (!r.get(k).valueAsString().equals(v) && !((Long)r.get(k).valueAsLong()).equals(Long.parseLong(v))) {
                    System.err.printf("Error, key: %s, expected value: %s, actural value: %s\n", k, v, r.get(k).valueAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws IOException, InterruptedException {
        long beginTime = System.currentTimeMillis();
        String folder = "D:\\middleware-data\\prerun_data\\prerun_data\\";
        List<String> orderFiles = Arrays.asList(folder + "order.0.0", folder + "order.0.3", folder + "order.1.1", folder + "order.2.2");
        List<String> goodFiles = Arrays.asList(folder + "good.0.0", folder + "good.1.1", folder + "good.2.2");
        List<String> buyerFiles = Arrays.asList(folder + "buyer.0.0", folder + "buyer.1.1");
        List<String> storeFolders = Arrays.asList("D:\\middleware-data\\prerun_data\\prerun_data\\store\\");

        OrderSystemImpl osi = new OrderSystemImpl();

        osi.construct(orderFiles, buyerFiles, goodFiles, storeFolders);
        System.out.printf("Construction complete, time: %dms\n", System.currentTimeMillis() - beginTime);
        beginTime = System.currentTimeMillis();
        int cnt = 0;
        String filename = "D:\\middleware-data\\prerun_data\\prerun_data\\case.0";
        try {

            FileInputStream fis = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            while (true) {
                ++cnt;
                if (cnt == 4) {
                    int a = 1;
                }
                String cs = br.readLine();
                if (cs == null) break;
                if (cs.contains("QUERY_ORDER")) {
                    long orderid = Long.parseLong(br.readLine().replace("ORDERID:", ""));
                    String[] keys = getKeys(br.readLine());
                    List<String> k = Arrays.asList(keys);
                    if (keys.length != 0 && keys[0].equals("*")) k = null;
                    OrderSystem.Result r = osi.queryOrder(orderid, k);
                    br.readLine();
                    while (true) {
                        String t = br.readLine();
                        if (t.equals("}")) break;
                        checkResult(t, r);
                    }
                    br.readLine();
                } else if (cs.contains("QUERY_BUYER_TSRANGE")) {
                    String buyerid = br.readLine().replace("BUYERID:", "");
                    long startTime = Long.parseLong(br.readLine().replace("STARTTIME:", ""));
                    long endTime = Long.parseLong(br.readLine().replace("ENDTIME:", ""));
                    Iterator<OrderSystem.Result> r = osi.queryOrdersByBuyer(startTime, endTime, buyerid);
                    String t = br.readLine();
                    while (true) {
                        t = br.readLine();
                        if (t.equals("}")) break;
                        OrderSystem.Result sr = r.next();
                        checkResult(t, sr);
                    }
                    if (r.hasNext()) {
                        System.err.println("Error, extra result found.");
                    }
                    br.readLine();
                } else if (cs.contains("QUERY_SALER_GOOD")) {
                    String salerid = br.readLine().replace("SALERID:", "");
                    String goodid = br.readLine().replace("GOODID:", "");
                    String[] keys = getKeys(br.readLine());
                    List<String> k = Arrays.asList(keys);
                    if (keys.length != 0 && keys[0].equals("*")) k = null;
                    Iterator<OrderSystem.Result> r = osi.queryOrdersBySaler(salerid, goodid, k);
                    br.readLine();
                    while (true) {
                        String t = br.readLine();
                        if (t.equals("}")) break;
                        OrderSystem.Result sr = r.next();
                        checkResult(t, sr);
                    }
                    if (r.hasNext()) {
                        System.err.println("Error, extra result found.");
                    }
                    br.readLine();
                } else {
                    String goodid = br.readLine().replace("GOODID:", "");
                    String[] keys = getKeys(br.readLine());
                    String sr = br.readLine().replace("RESULT:", "");
                    if (sr.contains("null")) {
                        sr = null;
                    }
                    OrderSystem.KeyValue kv = osi.sumOrdersByGood(goodid, keys[0]);
                    if (sr == null) {
                        if (kv != null) {
                            System.err.printf("Error when calculating sum, expected value: null, actural value: %f.\n", kv.valueAsString());
                        }
                        br.readLine();
                        continue;
                    }
                    String r = kv.valueAsString();
                    double dr = Double.parseDouble(r), dsr = Double.parseDouble(sr);
                    if (Math.abs(dr - dsr) > 0.0001) {
                        System.err.printf("Error when calculating sum, expected value: %f, actural value: %f.\n", dsr, dr);
                    }
                    br.readLine();
                }

                System.out.printf("Test case %d complete.\n", cnt);
            }
            System.out.printf("Query complete, time: %dms\n", System.currentTimeMillis() - beginTime);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
