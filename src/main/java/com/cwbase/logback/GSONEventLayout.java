package com.cwbase.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FIXME: update
 * Adapt from XMLLayout
 *
 * @author kmtong
 */
public class GSONEventLayout extends LayoutBase<ILoggingEvent> {
    private Pattern MDC_VAR_PATTERN = Pattern.compile("\\@\\{([^}^:-]*)(:-([^}]*)?)?\\}");

    private boolean locationInfo = false;
    private int callerStackIdx = 0;
    private boolean properties = false;

    String source;
    String sourceHost;
    String sourcePath;
    List<String> tags;
    List<AdditionalField> additionalFields;
    String type;

    private static final Gson gson = new GsonBuilder().create();

    public GSONEventLayout() {
        try {
            setSourceHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
        }
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By default, it is
     * set to false which means there will be no location information output by
     * this layout. If the the option is set to true, then the file name and
     * line number of the statement at the origin of the log statement will be
     * output.
     * <p>
     * <p>
     * If you are embedding this layout within an
     * <code>org.apache.log4j.net.SMTPAppender</code> then make sure to set the
     * <b>LocationInfo</b> option of that appender as well.
     */
    public void setLocationInfo(boolean flag) {
        locationInfo = flag;
    }

    /**
     * Returns the current value of the <b>LocationInfo</b> option.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * Sets whether MDC key-value pairs should be output, default false.
     *
     * @param flag new value.
     * @since 1.2.15
     */
    public void setProperties(final boolean flag) {
        properties = flag;
    }

    /**
     * Gets whether MDC key-value pairs should be output.
     *
     * @return true if MDC key-value pairs are output.
     * @since 1.2.15
     */
    public boolean getProperties() {
        return properties;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();

        Map<String, Object> map = new LinkedHashMap<>(); //FIXME: size, capacitate correctly, maybe make HashMap, give that a try!
        map.put("source", mdcSubst(source, mdc));
        map.put("host", mdcSubst(sourceHost, mdc));
        map.put("path", mdcSubst(sourcePath, mdc));
        map.put("type", mdcSubst(type, mdc));
        if (tags != null) {
            map.put("tags", mdcSubstList(tags, mdc));
        }
        map.put("message", event.getFormattedMessage());
        map.put("@timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeStamp()))); //FIXME: maybe use gson formatter for this

//        buf.append("{");
//        appendKeyValue(buf, "source", source, mdc);
//        buf.append(COMMA);
//        appendKeyValue(buf, "host", sourceHost, mdc);
//        buf.append(COMMA);
//        appendKeyValue(buf, "path", sourcePath, mdc);
//        buf.append(COMMA);
//        appendKeyValue(buf, "type", type, mdc);
//        buf.append(COMMA);
//        appendKeyValue(buf, "tags", tags, mdc);
//        buf.append(COMMA);
//        appendKeyValue(buf, "message", event.getFormattedMessage(), null);
//        buf.append(COMMA);
//        appendKeyValue(buf, "@timestamp",
//                DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeStamp())), null);
//        buf.append(COMMA);

        map.put("logger", event.getLoggerName());
        map.put("level", event.getLevel().toString());
        map.put("thread", event.getThreadName());

        // ---- fields ----
//        appendKeyValue(buf, "logger", event.getLoggerName(), null);
//        buf.append(COMMA);
//        appendKeyValue(buf, "level", event.getLevel().toString(), null);
//        buf.append(COMMA);
//        appendKeyValue(buf, "thread", event.getThreadName(), null);
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            //FIXME: almost assuredly a better way to format this than logback's util
//            buf.append(COMMA);
            String throwable = ThrowableProxyUtil.asString(tp);
//            appendKeyValue(buf, "throwable", throwable, null);
            map.put("throwable", throwable);
        }
        if (locationInfo) {
            StackTraceElement[] callerDataArray = event.getCallerData();
            if (callerDataArray != null
                    && callerDataArray.length > callerStackIdx) {
                Map<String, String> location = new LinkedHashMap<>(); //FIXME: size, maybe use HashMap, etc.

//                buf.append(COMMA);
//                buf.append("\"location\":{");
                StackTraceElement immediateCallerData = callerDataArray[callerStackIdx];
                location.put("class", immediateCallerData.getClassName());
                location.put("method", immediateCallerData.getMethodName());
                location.put("file", immediateCallerData.getFileName());
                location.put("line", Integer.toString(immediateCallerData.getLineNumber())); //FIXME: maybe make this an Object map and not toString this ourselves?

//                appendKeyValue(buf, "class",
//                        immediateCallerData.getClassName(), null);
//                buf.append(COMMA);
//                appendKeyValue(buf, "method",
//                        immediateCallerData.getMethodName(), null);
//                buf.append(COMMA);
//                appendKeyValue(buf, "file", immediateCallerData.getFileName(),
//                        null);
//                buf.append(COMMA);
//                appendKeyValue(buf, "line",
//                        Integer.toString(immediateCallerData.getLineNumber()),
//                        null);
//                buf.append("}");
                map.put("location", location);
            }
        }

		/*
         * <log4j:properties> <log4j:data name="name" value="value"/>
		 * </log4j:properties>
		 */
        if (properties && !mdc.isEmpty()) {
            map.put("properties", mdc);

//            if ((propertyMap != null) && (propertyMap.size() != 0)) {
//                Set<Entry<String, String>> entrySet = propertyMap.entrySet();
//                buf.append(COMMA);
//                buf.append("\"properties\":{");
//                Iterator<Entry<String, String>> i = entrySet.iterator();
//                while (i.hasNext()) {
//                    Entry<String, String> entry = i.next();
//                    appendKeyValue(buf, entry.getKey(), entry.getValue(), null);
//                    if (i.hasNext()) {
//                        buf.append(COMMA);
//                    }
//                }
//                buf.append("}");
//            }
        }

        //FIXME: replace this perhaps with a streams usage
        if (additionalFields != null) {
            for (AdditionalField field : additionalFields) {
                map.put(field.getKey(), mdcSubst(field.getValue(), mdc));
//                buf.append(COMMA);
//                appendKeyValue(buf, field.getKey(), field.getValue(), mdc);
            }
        }

//        buf.append("}");

//        return buf.toString();

        return gson.toJson(map);
    }

    //FIXME: rename, probably no way to improve this without some trickery
    private String mdcSubst(String v, Map<String, String> mdc) {
        if (mdc != null && v != null && v.contains("@{")) {
            Matcher m = MDC_VAR_PATTERN.matcher(v);
            StringBuffer sb = new StringBuffer(v.length());
            while (m.find()) {
                String val = mdc.get(m.group(1));
                if (val == null) {
                    // If a default value exists, use it
                    val = (m.group(3) != null) ? m.group(3) : m.group(1) + "_NOT_FOUND";
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(val));
            }
            m.appendTail(sb);
            return sb.toString();
        }
        return v;
    }

    //FIXME: rename, etc. etc.
    private List<String> mdcSubstList(List<String> strings, Map<String, String> mdc) {
        return strings.stream().map(it -> mdcSubst(it, mdc)).collect(Collectors.toList());
    }

    private String escape(String s) {
        if (s == null)
            return null;
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    if (ch <= '\u001F') {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }// for
        return sb.toString();
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCallerStackIdx() {
        return callerStackIdx;
    }

    /**
     * Location information dump with respect to call stack level. Some
     * framework (Play) wraps the original logging method, and dumping the
     * location always log the file of the wrapper instead of the actual caller.
     * For PlayFramework, I use 2.
     *
     * @param callerStackIdx
     */
    public void setCallerStackIdx(int callerStackIdx) {
        this.callerStackIdx = callerStackIdx;
    }

    public void addAdditionalField(AdditionalField p) {
        if (additionalFields == null) {
            additionalFields = new ArrayList<AdditionalField>();
        }
        additionalFields.add(p);
    }

}
