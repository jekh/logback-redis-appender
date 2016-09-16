package com.cwbase.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.junit.Test;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

public class JSONEventLayoutPerfTest {
    @Test
    public void testPerformance() {

        JSONEventLayout layout = new JSONEventLayout();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            String output = layout.doLayout(new MockLoggingEvent());
            if (output.length() > 500000) {
                System.out.println("huge!");
            }
        }

        long finish = System.currentTimeMillis();

        System.out.println("Time elapsed: " + (finish - start) + "ms");

    }

    public class MockLoggingEvent implements ILoggingEvent {
        private final Map<String, String> mdc;

        public MockLoggingEvent() {
            mdc = new HashMap<String, String>();
            mdc.put("key1", "value1");
        }


        public String getThreadName() {
            return "thread-1";
        }

        public Level getLevel() {
            return Level.INFO;
        }

        public String getMessage() {
            return "This is a simple logging message";
        }

        public Object[] getArgumentArray() {
            return new Object[0];
        }

        public String getFormattedMessage() {
            return "This is a simple logging message";
        }

        public String getLoggerName() {
            return "com.test.logger";
        }

        public LoggerContextVO getLoggerContextVO() {
            return null;
        }

        public IThrowableProxy getThrowableProxy() {
            return null;
        }

        public StackTraceElement[] getCallerData() {
            return new StackTraceElement[0];
        }

        public boolean hasCallerData() {
            return false;
        }

        public Marker getMarker() {
            return null;
        }

        public Map<String, String> getMDCPropertyMap() {
            return mdc;
        }

        public Map<String, String> getMdc() {
            return mdc;
        }

        public long getTimeStamp() {
            return System.currentTimeMillis();
        }

        public void prepareForDeferredProcessing() {

        }
    }
}
