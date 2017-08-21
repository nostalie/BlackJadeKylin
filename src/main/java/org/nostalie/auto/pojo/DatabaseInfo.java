package org.nostalie.auto.pojo;

/**
 * Created by nostalie on 17-8-18.
 */
public class DatabaseInfo {

    private static final String URL = "jdbc:mysql://%s:%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull";

    private int databaseId;
    private String host;
    private String port;
    private String userName;
    private String password;
    private String url;

    public String makeMySQLURL(){
        this.url = String.format(URL,this.host,this.port);
        return this.url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public String toString() {
        return "DatabaseInfo{" +
                "databaseId=" + databaseId +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
