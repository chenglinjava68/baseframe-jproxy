package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;

/**
 * 序列化对象的数据容器
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class DataContainer implements Serializable {

    private static final long serialVersionUID = -241895865673494738L;

    private Object data;

	public DataContainer() { }

	public DataContainer(Object data) {
	    this.data = data;
	}

	public Object getData() {
	    return data;
	}

	public void setData(Object data) {
	    this.data = data;
	}
}
