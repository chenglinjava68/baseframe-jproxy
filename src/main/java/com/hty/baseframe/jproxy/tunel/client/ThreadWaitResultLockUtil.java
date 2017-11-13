package com.hty.baseframe.jproxy.tunel.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ThreadWaitResultLockUtil {
	
	private static final Map<Thread, Object> locks = Collections.synchronizedMap(new HashMap<Thread, Object>());
	
	private static final Map<Thread, Object> result = Collections.synchronizedMap(new HashMap<Thread, Object>());
	
	private static final Map<Thread, Exception> except = Collections.synchronizedMap(new HashMap<Thread, Exception>());
	
	/**
	 * 获得当前线程的锁对象
	 * @return
	 */
	public static Object getThreadLock(Thread t) {
		Object lock = locks.get(t);
		if(null == lock) {
			lock = new Object();
			synchronized (locks) {
				locks.put(t, lock);
			}
		}
		return lock;
	}

	/**
	 * 根据锁对象获得Thread
	 * @param lock
	 * @return
	 */
	public static Thread getThread(Object lock) {
		synchronized (locks) {
			for (Iterator<Map.Entry<Thread, Object>> it = locks.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Thread, Object> entry = it.next();
				if(entry.getValue() == lock) {
					return entry.getKey();
				}
			}
			return null;
		}
	}
	/**
	 * 使当前线程等待，当远程处理完成或者发送异常，则唤醒当前线程。
	 * @return
	 * Object:远程处理相应结果对象，类型为ServiceResponse
	 * @throws Exception 
	 */
	public static Object waitThread() throws Exception {
		Thread t = Thread.currentThread();
		result.put(t, null);
		except.put(t, null);
		Object lock = locks.get(t);
		if(null == lock) {
			lock = new Object();
			synchronized (locks) {
				locks.put(t, lock);
			}
		}
		synchronized (lock) {
			lock.wait();
		}
		if(except.get(t) != null) {
			throw except.get(t);
		}
		return result.get(t);
	}
	/**
	 * 唤醒正在等待远程结果的线程
	 * @param lock 线程锁对象
	 */
	public static void notifyThread(Object lock, Object response, Exception ex) {
		try {
			Thread t = getThread(lock);
			result.put(t, response);
			except.put(t, ex);
			synchronized (lock) {
				lock.notify();
			}
		} catch (IllegalMonitorStateException e) {
			e.printStackTrace();
		}
	}
}
