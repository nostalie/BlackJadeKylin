package org.nostalie.auto.log.analysis;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author nostalie 17-12-4 下午4:55.
 */
public class FlightDetailProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightDetailProvider.class);
    private static final String PATTERN = "^(.*)IFlightDetailBatchService(.*)\"codes\":\"([A-Za-z0-9]+)\",\"depCity\":\"([A-Za-z]{3})\",\"arrCity\":\"([A-Za-z]{3})\",\"transfers\":\"([A-Za-z,]*)\",\"dates\":\"([0-9-]{10})\"([\\s\\S]*)Return value\\(\\{\"([0-9]*)\":\\[(.*)]}\\)";
    private static final Pattern PATTERN_CONDITION = Pattern.compile(PATTERN);
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final String PATH = "classpath:/flight_detail_current";

    public static void main(String[] args) {

    }

    private static List<File> getAllFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            assert files != null;
            return Lists.newArrayList(files);
        }
        return Lists.newArrayList();
    }

    private static void processFiles(List<File> files, Map<String, Integer> empty, Map<String, Integer> notEmpty) throws IOException {
        for(File file : files){
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream in = new GZIPInputStream(fileInputStream);
            Scanner sc=new Scanner(in);
            try {
                while(sc.hasNextLine()){
                    String line = sc.nextLine();
                    if(sc.hasNextLine()){
                        line += sc.nextLine();
                    }
                    put(empty,notEmpty,line);
                }
            } finally {
                fileInputStream.close();
                in.close();
                sc.close();
            }
        }
    }

    private static void put(Map<String, Integer> empty, Map<String, Integer> notEmpty, String line) {
        Matcher matcher = PATTERN_CONDITION.matcher(line);
        if (matcher.find()) {
            String condition = matcher.group(3) + "-" + matcher.group(4) + "-" + matcher.group(5) + "-" + matcher.group(6) + "-" + matcher.group(7);
            String value = matcher.group(10);
            if (value.equals("")) {
                if (empty.get(condition) == null) {
                    empty.put(condition, 1);
                } else {
                    int v = empty.get(condition) + 1;
                    empty.put(condition, v);
                }
            } else {
                if (notEmpty.get(condition) == null) {
                    empty.put(condition, 1);
                } else {
                    int v = notEmpty.get(condition) + 1;
                    empty.put(condition, v);
                }
            }
            int count = COUNT.addAndGet(1);
            LOGGER.info("handling " + count);
        } else {
            LOGGER.error("匹配失败");
        }
    }


}
