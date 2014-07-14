package com.yxl.heartbeat;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.yxl.thread.ClientThread;

/**
 * 客户端心跳工具类
 * 
 * @author yuanxiaolong.sam
 * 
 * 
 */
public class ClientHeartbeatUtil {
	
	//发送频率
	private int frefrequency = 3000;
	
	public ClientHeartbeatUtil(int frefrequency) {
		this.frefrequency = frefrequency;
	}

	// 启动心跳,为了让2个client公用此类,入参为client Name
	public void start(String clientName) {
		while (true) {
			try {
				Thread.sleep(frefrequency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 根据长短连接决定怎样发送消息
			if (ClientThread.isShortTcp) {
				sendMsgByShortTcp(clientName);
			} else {
				sendMsgByLongTcp(clientName);
			}
		}
	}

	// 发送消息,利用长连接
	private void sendMsgByLongTcp(String clientName) {
		if (ClientThread.channelFuture == null) {
			return;
		}
		Channel channel = ClientThread.channelFuture.getChannel();
		if (channel != null && channel.isOpen() && channel.isConnected()
				&& channel.isWritable()) {
			System.out.println("客户端利用连接: " + channel + " 发送消息");
			String s = clientName + " say Hello";
			channel.write(s);
		}
	}

	// 发送消息,利用短连接
	private void sendMsgByShortTcp(String clientName) {
		ChannelFuture future = ClientThread.bootstrap
				.connect(ClientThread.remoteServerAddress);

		future.awaitUninterruptibly();// 这里为了简便,调用此方法。更好的方法应该是加listener

		Channel channel = future.getChannel();
		if (channel != null && channel.isOpen() && channel.isConnected()
				&& channel.isWritable()) {
			System.out.println("客户端建立短连接: " + channel + " 发送消息");
			String s = clientName + " say Hello";
			channel.write(s);
		}
	}

}
