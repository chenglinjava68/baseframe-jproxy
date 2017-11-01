package com.hty.baseframe.jproxy.util;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.Delegate;
import com.dyuproject.protostuff.runtime.RuntimeEnv;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.hty.baseframe.jproxy.bean.DataContainer;
import com.hty.baseframe.jproxy.bean.TimestampDelegate;

/**
 * ProtoStuff序列化工具类。
 */
@SuppressWarnings("unchecked")
public class SerializeUtil {
    //用于缓存Protostuff的Schema信息
    private static final Map<Class<?>, RuntimeSchema<?>> schemas =
            new HashMap<Class<?>, RuntimeSchema<?>>();
    public static final int LINK_BUFFER_SIZE = 256;
   
    
    /** 时间戳转换Delegate，解决时间戳转换后错误问题  */
    private final static Delegate<Timestamp> TIMESTAMP_DELEGATE = new TimestampDelegate();

    private final static DefaultIdStrategy idStrategy = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);

    static {
        idStrategy.registerDelegate(TIMESTAMP_DELEGATE);
    }
    
    
    /**
     * 从schemas集合获取类的RuntimeSchema，如果不存在，则创建，加入schemas并返回
     * @param type
     * @param <T>
     * @return
     */
    public static <T> RuntimeSchema<T> getSchema(Class<T> type) {
		RuntimeSchema<T> schema = (RuntimeSchema<T>) schemas.get(type);
        if(null == schema) {
            schema = RuntimeSchema.createFrom(type, idStrategy);
            schemas.put(type, schema);
        }
        return schema;
    }

    /**
     * 将一个对象反序列化为字节数组
     * @param obj
     * @return
     */
    public static byte[] serialize(Object obj) {
        DataContainer objContainer = new DataContainer(obj);
        RuntimeSchema<DataContainer> schema = getSchema(DataContainer.class);
        LinkedBuffer buffer = LinkedBuffer.allocate(LINK_BUFFER_SIZE);
        byte[] obj_bs = ProtostuffIOUtil.toByteArray(objContainer, schema, buffer);
        buffer.clear();
        buffer = null;
        return obj_bs;
    }

    /**
     * 将字节数组序列化为指定类型的类
     * @param obj_bs
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] obj_bs, Class<T> type) {
        DataContainer valueContainer = new DataContainer();
        RuntimeSchema<DataContainer> schema = getSchema(DataContainer.class);
        if(null != obj_bs && obj_bs.length > 0)
            ProtostuffIOUtil.mergeFrom(obj_bs, valueContainer, schema);
        return (T) valueContainer.getData();
    }
    /**
     * 用固定长度的字符串表示字节数据长度
     * @param len
     * @param width
     * @return
     */
    public static String getHeadString(int len, int width) {
    	if(len > 99999999 || len < 0) {
    		return "00000000";
    	}
    	String strlen = String.valueOf(len);
    	int padding = width - strlen.length();
    	for(int i = 0; i < padding; i++) {
    		strlen = "0" + strlen;
    	}
    	return strlen;
    }
}
