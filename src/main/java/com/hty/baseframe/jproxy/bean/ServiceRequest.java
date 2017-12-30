package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * Service请求Bean
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class ServiceRequest implements Serializable {

	private static final long serialVersionUID = 3249474311510602119L;

	/** 请求ID */
	private String requestId;
	/** 远程服务接口类 */
	private Class<?> clazz;
	/** 调用接口方法 */
	private MethodEntity methodEntity;
	/** 调用额外参数 */
	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	/** 添加额外参数 */
	public void addParameter(String key, Object value) {
		this.parameters.put(key, value);
	}
	/** 获取额外参数 */
	public Object getParameter(String key) {
		return this.parameters.get(key);
	}
	
	public MethodEntity getMethodEntity() {
		return methodEntity;
	}
	public void setMethodEntity(MethodEntity methodEntity) {
		this.methodEntity = methodEntity;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public Class<?> getClazz() {
		return clazz;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
}
