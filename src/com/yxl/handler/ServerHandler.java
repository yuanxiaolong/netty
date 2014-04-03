package com.yxl.handler;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.yxl.main.ServerMain;

/**
 * Server处理程序Handler
 * 
 * @author yuanxiaolong.sam
 * 
 * 
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {

	// Server收到Client消息处理 
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		if (channel != null) {
			String host = ((InetSocketAddress) ctx.getChannel()
					.getRemoteAddress()).getAddress().getHostAddress();
			int port = ((InetSocketAddress) ctx.getChannel().getRemoteAddress())
					.getPort();
			//将ip和host组装起来,放到map里,用于管理多个Client的连接
			ServerMain.channelMap.put(host + ":" + port, channel);
		}
		System.out.println("Server recive message: " + e.getMessage());
		//查看当前连接Map
		System.out.println(ServerMain.channelMap);
	}

	// 异常处理回调
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		System.err.println(" Server has a error,Error cause:" + e.getCause());
		e.getChannel().close();
	}

	// 连接关闭处理
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		String host = ((InetSocketAddress) ctx.getChannel()
				.getRemoteAddress()).getAddress().getHostAddress();
		int port = ((InetSocketAddress) ctx.getChannel().getRemoteAddress())
				.getPort();
		System.out.println("server close channel: " + "[" + host + ":" + port + "]");
		//移除无用连接
		try {
			ServerMain.channelMap.remove(host + ":" + port);
		} catch (Exception e2) {
			//这里如果为了更高可用,可以将删除失败的加入一个队列里，后台启动一个cleanup的线程
		}
	}

}
