/*
 * @(#) ESDataManage
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author Administrator
 * <br> 2019-11-26 19:42:02
 */

package com.huaban.analysis.jieba;

import com.huaban.analysis.jieba.similarity.CosineTextSimilarity;
import com.huaban.analysis.jieba.similarity.Hit;
import com.huaban.analysis.jieba.similarity.Hits;
import com.huaban.analysis.jieba.similarity.SimilarityRanker;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class ESDataManage {
    static String  driver = "org.elasticsearch.xpack.sql.jdbc.jdbc.JdbcDriver";
    static String elasticsearchAddress = "localhost:9200";

    public static Properties connectionProperties(){
        Properties properties = new Properties();
//        properties.put("user", "test_admin");
//        properties.put("password", "x-pack-test-password");
        return properties;
    }

    /**
     * 从es获取事件集合
     * @return
     */
    @Test
    public void getEventDataList(){
        String address = "jdbc:es://http://" + elasticsearchAddress;
        Properties connectionProperties = connectionProperties();
        List<Map<String,String>> list=new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(address, connectionProperties);
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                //"SELECT name, word FROM test where  match(word, '今天 or 慌 or 暴雨')");
                "SELECT * from t_event_basic_info");
            System.out.println(results);


            while (results.next()) {
                Map<String ,String > map=new HashMap<>();
                System.out.println(results.getString("event_content"));
                map.put("event_id",results.getString("event_id"));
                map.put("event_content",results.getString("event_content"));
                list.add(map);
            }
            System.out.println(list.toArray());

            similarity(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算时间相似度
     * @param list
     */
    public void similarity(List<Map<String,String>> list){
        SimilarityRanker similarityRanker=new CosineTextSimilarity();
// 1.创建RestClient对象x
        RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("localhost", 9200, "http")));
        for(int i=0;i<list.size();i++){
            String sourceId=list.get(i).get("event_id");
            String sourceContent=list.get(i).get("event_content");
            Hits hits=similarityRanker.rank(sourceId,sourceContent,list,Integer.MAX_VALUE);
            System.out.println(hits.toString());
            insertES(hits,client);
        }
    }

    public void insertES(Hits hits,RestHighLevelClient client){
        try {
            BulkRequest request = new BulkRequest();
            for(Hit hit : hits.getHits()){
                String a=UUID.randomUUID().toString();
                IndexRequest indexRequest=new IndexRequest("similarity","doc",a);
                indexRequest.source("id", a,"score",hit.getScore(),
                    "eventId",hit.getEventId(),
                    "sourceId",hit.getSourceId());
                    request.add(indexRequest);
            }

            // 2、可选的设置
           /*
           request.timeout("2m");
           request.setRefreshPolicy("wait_for");
           request.waitForActiveShards(2);
           */
            //3、发送请求
            // 同步请求
            BulkResponse bulkResponse = client.bulk(request);
            //4、处理响应
            if(bulkResponse != null) {
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                    if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        //TODO 新增成功的处理
                        System.out.println("新增成功,{}"+ indexResponse.toString());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        //TODO 修改成功的处理
                        System.out.println("修改成功,{}"+ updateResponse.toString());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                        //TODO 删除成功的处理
                        System.out.println("删除成功,{}"+ deleteResponse.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void insert(){
        try {
            // 1.创建RestClient对象x
            RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost("localhost", 9200, "http")));
            BulkRequest request = new BulkRequest();
                int i=1;
                IndexRequest indexRequest=new IndexRequest("similarity","doc","2");
                indexRequest.source("id", UUID.randomUUID().toString(),"score","2.32",
                    "eventId","999",
                    "sourceId","888");
                request.add(indexRequest);

            // 2、可选的设置
           /*
           request.timeout("2m");
           request.setRefreshPolicy("wait_for");
           request.waitForActiveShards(2);
           */
            //3、发送请求
            // 同步请求
            BulkResponse bulkResponse = client.bulk(request);
            //4、处理响应
            if(bulkResponse != null) {
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                    if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        //TODO 新增成功的处理
                        System.out.println("新增成功,{}"+ indexResponse.toString());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        //TODO 修改成功的处理
                        System.out.println("修改成功,{}"+ updateResponse.toString());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                        //TODO 删除成功的处理
                        System.out.println("删除成功,{}"+ deleteResponse.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
