package com.dianping.cat.status;


import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by liyinhao on 18/7/18.
 */
public class JMXQuery {

    private static MBeanServer mbs;

    static {
        try {
            mbs = ManagementFactory.getPlatformMBeanServer();
        } catch (Exception e) {
            // ignore
        }
    }

    public static List<Map<String, Object>> queryList(String category, String typeName, String[] attributes) {
        return queryList(category, null, typeName, attributes);
    }

    public static Map<String, Map<String, Object>> queryMap(String category, String typeName, String[] attributes) {
        return queryMap(category, null, typeName, attributes);
    }

    private static Map<String, Map<String, Object>> queryMap(String category, String objNamePattern, String typeName,
                                                             String[] attributes) {

        Map<String, Map<String, Object>> rtn = new HashMap<String, Map<String, Object>>();

        try {

            ObjectName pool = new ObjectName(category + ":type=" + typeName + ",*");
            Set<ObjectName> names = mbs.queryNames(pool, null);

            for (ObjectName name : names) {

                if (objNamePattern != null && !Pattern.matches(objNamePattern, name.getCanonicalName())) {
                    continue;
                }

                try {

                    Map<String, Object> attrs = queryAttribute(name, attributes);
                    rtn.put(name.getKeyProperty("name"), attrs);
                } catch (Throwable e) {
                    continue;
                }
            }
        } catch (Throwable e) {
            return rtn;
        }

        return rtn;
    }

    private static List<Map<String, Object>> queryList(String category, String objNamePattern, String typeName,
                                                       String[] attributes) {

        List<Map<String, Object>> rtn = new ArrayList<Map<String, Object>>();

        try {

            ObjectName pool = new ObjectName(category + ":type=" + typeName + ",*");
            Set<ObjectName> names = mbs.queryNames(pool, null);

            for (ObjectName name : names) {

                if (objNamePattern != null && !Pattern.matches(objNamePattern, name.getCanonicalName())) {
                    continue;
                }

                try {
                    Map<String, Object> attrs = queryAttribute(name, attributes);
                    rtn.add(attrs);
                } catch (Throwable e) {
                    continue;
                }
            }
        } catch (Throwable e) {
            // ignore
        }

        return rtn;

    }

    private static Map<String, Object> queryAttribute(ObjectName name, String[] attributes) throws Exception {

        Map<String, Object> result = new HashMap<String, Object>();

        AttributeList list = mbs.getAttributes(name, attributes);
        for (Attribute a : list.asList()) {

            Object attrVal = a.getValue();
            if (attrVal instanceof CompositeDataSupport) {

                CompositeDataSupport dataSupport = (CompositeDataSupport) attrVal;
                result.put(a.getName(), dataSupport.values());
            } else {

                result.put(a.getName(), attrVal);
            }
        }

        return result;
    }
}
