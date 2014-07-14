package com.yxl.handler;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import com.yxl.thread.ClientThread;

/**
 * 客户端处理handler 
 * @author yuanxiaolong.sam 
 */
public class ClientHanlder extends SimpleChannelUpstreamHandler {

	//轮询定时器
	private static Timer timer = new HashedWheelTimer();
	
	//定时任务逻辑相同,静态化减少开销
	private static TimerTask timerTask = new TimerTask() {
		@Override
		public void run(Timeout timeout) throws Exception {
			//定时器回调逻辑,即重连,并将重连后的future设置到客户端线程里
			ChannelFuture channelFuture =  ClientThread.bootstrap.connect(ClientThread.remoteServerAddress);
			ClientThread.channelFuture = channelFuture;
		}
	};
	
	private static final int RETRY_TIME = 5;
	
	//客户端接收到服务器发送的消息处理回调函数
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		System.out.println("Client Received msg: " + e.getMessage());
		//如果是短连接,则关闭channel
		if (ClientThread.isShortTcp) {
			System.out.println("短连接,now close channel: " + ctx.getChannel());
			ctx.getChannel().close();
		}
	}
 
	//异常回调函数
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		System.err.println("Client has a error ,Error cause:" + e.getCause());
		e.getChannel().close();//有异常则关闭连接
	}
	
	//channel关闭回调函数
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e){
        System.out.println("channel has closed,retry after 5's ");
        
        //如果是长连接,才需要客户端断线重连
        if (!ClientThread.isShortTcp) {
        	//设定定时器,5秒后执行一次逻辑
            timer.newTimeout(timerTask, RETRY_TIME, TimeUnit.SECONDS);
		}
       
    }
}
