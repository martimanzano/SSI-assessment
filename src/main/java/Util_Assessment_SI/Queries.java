package Util_Assessment_SI;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static Util_Assessment_SI.BayesUtils.QMtoID;
import static Util_Assessment_SI.Constants.*;

public class Queries {
    public static String getStringFromMap(Map<String, Object> map, String k) {
        return String.valueOf(map.get(k));
    }

    private static String getIndexPath(String element_index_name, String projectId) {
        String index= PATH + INDEX_PREFIX + element_index_name;
        if (projectId!=null && !projectId.isEmpty() && !projectId.equalsIgnoreCase("EMPTY")&&
                !projectId.equalsIgnoreCase("\"\""))
            index=index.concat("."+projectId);
        return index;
    }
    private static String getFactorsIndex(String projectId) {
        return getIndexPath(INDEX_FACTORS, projectId);
    }

    private static String getStrategicIndicatorsIndex(String projectId) {
        return getIndexPath(INDEX_STRATEGIC_INDICATORS, projectId);
    }
    private static String getMetricsIndex(String projectId)
    {
        return getIndexPath(INDEX_METRICS, projectId);
    }
    private static String getRelationsIndex(String projectId)
    {
        return getIndexPath(INDEX_RELATIONS, projectId);
    }
    public static String formatDate(LocalDate day) {
        return day.toString();
    }

    public static SearchResponse getFrequencies(String projectId,Constants.QMLevel QMLevel, String name,
                                                LocalDate dateFrom, LocalDate dateTo, double[] ranges) throws IOException {
        RestHighLevelClient client = Connection.getConnectionClient();
        return client.search(new SearchRequest(getIndex(projectId, QMLevel))
                .source(new SearchSourceBuilder()
                        .query(QueryBuilders.boolQuery()
                                .must(new TermQueryBuilder(QMtoID(QMLevel), name))
                                .filter(QueryBuilders
                                        .rangeQuery(Constants.EVALUATION_DATE)
                                        .gte(dateFrom)
                                        .lte(dateTo)))
                        .size(0)
                        .aggregation(getRangeAggregation(ranges)))
        );
    }

    public static SearchResponse getElementsAggregations(String projectId, Constants.QMLevel QMType, String[] elementNames,
                                                        LocalDate dateFrom, LocalDate dateTo) throws IOException {
        RestHighLevelClient client = Connection.getConnectionClient();
        return client.search(new SearchRequest(getIndex(projectId, QMType))
                .source(new SearchSourceBuilder()
                        .fetchSource(Constants.VALUE, null)
                        .sort(Constants.VALUE, SortOrder.ASC)
                        .query(QueryBuilders.boolQuery()
                                .should(new TermsQueryBuilder(QMtoID(QMType), elementNames)).minimumShouldMatch(1)
                                .filter(QueryBuilders
                                        .rangeQuery(Constants.EVALUATION_DATE)
                                        .gte(dateFrom)
                                        .lte(dateTo)))
                        .size(10000))
        );
    }

    public static SearchResponse getFilteredDay(String projectId, Constants.QMLevel QMLevel, LocalDate day,
                                                String[] names) throws IOException {
        TermQueryBuilder mustDay = new TermQueryBuilder(Constants.EVALUATION_DATE, formatDate(day));
        BoolQueryBuilder shouldNames = QueryBuilders.boolQuery();
        for (String name : names) {
            shouldNames.should(new TermQueryBuilder(QMtoID(QMLevel), name));
        }

        RestHighLevelClient client = Connection.getConnectionClient();
        return client.search((new SearchRequest(getIndex(projectId, QMLevel))
                .source(new SearchSourceBuilder()
                        .query(QueryBuilders.boolQuery()
                                .must(mustDay)
                                .must(shouldNames)))));
    }

    public static AggregationBuilder getRangeAggregation(double[] ranges) {
        RangeAggregationBuilder range = AggregationBuilders.range("categoryranges").field("value")
                //.addUnboundedTo(ranges[0]);
                .addRange(0f, ranges[0]);
        for (int i = 0; i < ranges.length-1; i++ ) {
            range.addRange(ranges[i], ranges[i+1]);
        }
        //range.addUnboundedFrom(ranges[ranges.length-1]);
        range.addRange(ranges[ranges.length-1], 1.01f);
        return range;
    }


    private static String getIndex(String projectId, QMLevel QMLevel) {
        String index="";
        switch (QMLevel) {
            case strategic_indicators:
                index = getStrategicIndicatorsIndex(projectId);break;
            case factors:
                index = getFactorsIndex(projectId);break;
            case metrics:
                index = getMetricsIndex(projectId);break;
            case relations:
                index = getRelationsIndex(projectId);break;
        }
//        System.out.println("GET INDEX: " + index);
        return index;
    }

}



