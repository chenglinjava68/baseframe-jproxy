package com.hty.baseframe.jproxy.util;

import com.hty.baseframe.common.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConditionMatchUtil {
    /**
     * 判断Service是否满足给定的条件
     * @param requestConditions
     * @param initialConditions
     * @return
     */
    public static boolean isMatch(Map<String, String> requestConditions,
                           Map<String, String> initialConditions) {
        if(null == requestConditions || requestConditions.isEmpty()) {
            return true;
        }
        if(null == initialConditions || initialConditions.isEmpty()) {
            return false;
        }
        boolean matchAll = true;
        for(Iterator<String> it = requestConditions.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            String reqValue = requestConditions.get(key);
            String initValue = initialConditions.get(key);
            if(StringUtil.isEmpty(reqValue) && StringUtil.isEmpty(initValue)) {
                continue;
            }
            if(!reqValue.equalsIgnoreCase(initValue)) {
                matchAll = false;
                break;
            }
        }
        return matchAll;
    }

    /**
     * 判断条件的包含关系<br>
     * 0：相等<br>
     * 1:1包含2<br>
     * 2:2包含 1<br>
     * -1:互不包含
     * @param cds1 map1
     * @param cds2 map2
     * @return
     */
    public static int mapCompare(Map<String, String> cds1,
                          Map<String, String> cds2) {
        if((null == cds1 || cds1.isEmpty()) &&
                (null == cds2 || cds2.isEmpty()))
            return 0;
        if(null == cds1 && null != cds2) {
            return 2;
        }
        if(null == cds2 && null != cds1) {
            return 1;
        }
        if(cds1.size() > cds2.size()) {
            if(isMatch(cds2, cds1)) {
                return 1;
            } else {
                return -1;
            }
        }
        else if(cds2.size() > cds1.size()) {
            if(isMatch(cds1, cds2)) {
                return 2;
            } else {
                return -1;
            }
        }
        else {
            if(isMatch(cds1, cds2)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public static void main(String[] args) {
        Map<String, String> map1 = new HashMap<String, String>();
        Map<String, String> map2 = new HashMap<String, String>();

        map1.put("a", "1");
        map1.put("b", "2");
        map1.put("c", "3");

        map2.put("a", "1");
        map2.put("b", "2");
        map2.put("e", "3");

        System.out.println(mapCompare(map1, map2));
    }
}
