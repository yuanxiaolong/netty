package com.yxl.heartbeat;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.yxl.thread.ClientThread;

/**
 * 客户端心跳工具类
 * @author yuanxiaolong.sam 
 *
 * 
 */
public class ClientHeartbeatUtil {

	//启动心跳,为了让2个client公用此类,入参为client Name
	public static void start(String clientName) {
		while (true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sendMsg(ClientThread.channelFuture,clientName);
		}
	}
	
	//发送消息
	private static boolean sendMsg(ChannelFuture channelFuture,String clientName) {
		if (channelFuture == null) {
			return false;
		}
		Channel channel = channelFuture.getChannel();
		if (channel != null && channel.isOpen() && channel.isConnected()
				&& channel.isWritable()) {
			String s = clientName + " say Hello";
			channel.write(s);
			return true;
		}
		return false;
	}

}
