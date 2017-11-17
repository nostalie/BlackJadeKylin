package org.nostalie.auto.mysql.datasource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author nostalie 17-10-12.
 */
public class DataSourceManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManagement.class);

    private static final String MYSQL_URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";
    private static final Joiner JOINER_DASH = Joiner.on("_");
    private static final Splitter SPLITTER_DASH = Splitter.on("_").trimResults();
    private static final Splitter SPLITTER_DOUBLE_SLASH = Splitter.on("//");
    private static final Splitter SPLITTER_COLON = Splitter.on(":");

    public static final LoadingCache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, DataSource>() {
                @Override
                public DataSource load(String key) throws Exception {
                    return createDataSource(key);
                }
            });

    public static void registeDataSource(Map<String, String> properties) throws Exception {
        try {
            DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
            String url = properties.get(DruidDataSourceFactory.PROP_URL);
            String userName = properties.get(DruidDataSourceFactory.PROP_USERNAME);
            String password = properties.get(DruidDataSourceFactory.PROP_PASSWORD);
            dataSourceCache.put(makeKey(url,userName,password),dataSource);
        } catch (Exception e) {
            LOGGER.error("数据源注册失败，properties:{}",properties);
            throw new RuntimeException("数据源注册失败",e);
        }
    }

    private static DataSource createDataSource(String key) throws Exception {
        Map<String, String> properties = Maps.newHashMap();
        List<String> param = SPLITTER_DASH.splitToList(key);
        if (param.size() == 3) {
            properties.put(DruidDataSourceFactory.PROP_URL, param.get(0));
            properties.put(DruidDataSourceFactory.PROP_USERNAME, param.get(1));
            properties.put(DruidDataSourceFactory.PROP_PASSWORD, param.get(2));
            return BasicDataSourceFactory.createDataSource(properties);
        }
        throw new RuntimeException("不是有效的key,创建data source失败");
    }

    /**
     * cache中的key形式： databaseName_tableName_userName_password
     */
    public static String makeKey(String url, String userName, String password) {
        return JOINER_DASH.join(url, userName, password);
    }
}
