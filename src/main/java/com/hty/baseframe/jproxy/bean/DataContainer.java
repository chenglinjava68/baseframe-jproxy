package com.hty.baseframe.jproxy.bean;
/**
 * 序列化对象的数据容器
 * @author Tisnyi
 * @version 1.0
 */
public class DataContainer {
	public DataContainer() { }
	private Object data;
	public DataContainer(Object data) { this.data = data; }
	public Object getData() { return data; }
	public void setData(Object data) { this.data = data; }
}
