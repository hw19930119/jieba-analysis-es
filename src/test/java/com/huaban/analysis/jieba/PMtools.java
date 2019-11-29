/*
 * @(#) PMtools
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-28 21:49:22
 *
 */

package com.huaban.analysis.jieba;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PMtools {

    public static void main(String[] args) {
        String mobileRegex = "(\\+\\d+)?1[3458]\\d{9}";//手机号
        String phoneRegex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}";//固定电话
        String sfzhRegex = "[1-9]\\d{16}[a-zA-Z0-9]{1}"; //身份证号
        String numRegex = "^\\d{1,6}$";

        System.out.println("2014".matches(numRegex));
        // String carnumRegex = "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(\\s*)(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(\\s*)[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})";
        Pattern pattern = Pattern.compile(phoneRegex);

        String s = "大渡口区春晖路街道涟水交警接警后，发现现场除了受害者李梅上官风，身份证号500102198702014291，电话18602314263，固定电话68574345,023-87232323,0571-8574432,(023)67232323";
        Matcher matcher = pattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            System.out.println(matcher.group() + ":" + matcher.start() + "-" + matcher.end());
            int length = matcher.end() - matcher.start();
            String fill = Stream.iterate("#", i -> i).limit(length).collect(Collectors.joining());
            matcher.appendReplacement(sb, fill);
        }
        matcher.appendTail(sb);


        System.out.println(sb.toString());
    }

    public class Data1 {
        private int id;
        private String name;
        private int amount;

        public Data1(int id, String name, int amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAmount() {
            return amount;
        }
    }

    public class Data2 {
        private int id;
        private String name;
        private String type;

        public Data2(int id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    public class OutputData {
        private int id;
        private String name;
        private String type;
        private int amount;

        public OutputData(int id, String name, String type, int amount) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.amount = amount;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }
    }


    public void intersectByKeyTest() {
        List<Data2> listOfData2 = new ArrayList<Data2>();

        listOfData2.add(new Data2(10501, "JOE", "Type1"));
        listOfData2.add(new Data2(10603, "SAL", "Type5"));
        listOfData2.add(new Data2(40514, "PETER", "Type4"));
        listOfData2.add(new Data2(59562, "JIM", "Type2"));
        listOfData2.add(new Data2(29415, "BOB", "Type1"));
        listOfData2.add(new Data2(61812, "JOE", "Type9"));
        listOfData2.add(new Data2(98432, "JOE", "Type7"));
        listOfData2.add(new Data2(62556, "JEFF", "Type1"));
        listOfData2.add(new Data2(10599, "TOM", "Type4"));


        List<Data1> listOfData1 = new ArrayList<Data1>();

        listOfData1.add(new Data1(10501, "JOE", 3000000));
        listOfData1.add(new Data1(10603, "SAL", 6225000));
        listOfData1.add(new Data1(40514, "PETER", 2005000));
        listOfData1.add(new Data1(59562, "JIM", 3000000));
        listOfData1.add(new Data1(29415, "BOB", 3000000));

        List<OutputData> result = listOfData1.stream()
            .flatMap(x -> listOfData2.stream()
                .filter(y -> x.getId() == y.getId())
                .map(y -> new OutputData(y.getId(), x.getName(), y.getType(), x.getAmount())))
            .collect(Collectors.toList());
        System.out.println(result);

        /*difference by key*/
        List<Data1> data1IntersectResult = listOfData1.stream().filter(data1 -> listOfData2.stream().map(Data2::getId).collect(Collectors.toList()).contains(data1.getId())).collect(Collectors.toList());
        System.out.println(data1IntersectResult);
    }
}
