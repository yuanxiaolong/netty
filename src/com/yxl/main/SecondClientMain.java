package com.yxl.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yxl.heartbeat.ClientHeartbeatUtil;
import com.yxl.thread.ClientThread;


/**
 * 第二个客户端
 * @author yuanxiaolong.sam 
 *
 * 
 */
public class SecondClientMain {
	
	
	//单线程池,只与一个Server连接,减少开销
	private static final ExecutorService service = Executors.newSingleThreadExecutor();
	
	public static void main(String[] args) {
		
		try {
			ClientThread clientThread = new ClientThread("client2");
			service.submit(clientThread);
			//启动心跳
			ClientHeartbeatUtil.start(clientThread.getName());
		} catch (Exception e) {
			System.out.println("unknow exception: " + e.getMessage());
		}
		

	}
}
