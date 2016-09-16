package com.cwbase.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfTest {
    public static final int parallel = 50;
    public static final AtomicInteger iterationsLeft = new AtomicInteger(10000);

    public static void main(String[] args) {
        try {
            invoke();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void invoke() throws InterruptedException {
//        makeLoggerString();
//        System.exit(0);

        String key = "logstash";
        Jedis redis;

        System.out.println("Before Test, clearing Redis");
        JedisPool pool = new JedisPool("localhost");
        redis = pool.getResource();
        // clear the redis list first
        redis.ltrim(key, 1, 0);

        configLogger("/logback-mdc-noconsole.xml");
        final Logger logger = LoggerFactory.getLogger(RedisAppenderTest.class);


        ExecutorService executorService = Executors.newFixedThreadPool(parallel);

        Runnable task = new Runnable() {
            public void run() {
                int iteration;
                while ((iteration = iterationsLeft.getAndDecrement()) > 0) {
                    MDC.put("mdcvar1", "test1");
                    MDC.put("mdcvar2", "test2");
                    logger.debug("Test MDC Log {}", iteration);
                }
            }
        };

        long start = System.currentTimeMillis();

        for (int i = 0; i < parallel; i++) {
            executorService.execute(task);
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        long finish = System.currentTimeMillis();

        // probably not immediately have the same size
        long size0 = redis.llen(key);
        System.out.println("Log Size: " + size0);
//        assertTrue(size0 < 100);

        Thread.sleep(2000);

        long size1 = redis.llen(key);
        System.out.println("Log Size After Wait: " + size1);
//        assertTrue(size0 < size1);

        System.out.println("Time to complete: " + (finish - start) + "ms");
    }

    protected static void configLogger(String loggerxml) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(PerfTest.class.getResourceAsStream(loggerxml));
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public static void makeLoggerString() {
        Map<String, Object> embed = new HashMap<>();
        embed.put("embedone", "embedval");

        Map<String, Object> myMap = new HashMap<>();
        myMap.put("one", "hello");
        myMap.put("two", "world");
        myMap.put("three", embed);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(myMap);

        System.out.println(json);
    }
}
