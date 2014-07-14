package com.yxl.thread;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;

import com.yxl.handler.ClientHanlder;

/**
 * 启动client线程,用于初始化连接
 * @author yuanxiaolong.sam 
 *
 *
 */
public class ClientThread implements Runnable {
	
	public static boolean isShortTcp = false;

	//创建Client端线程池工厂
	private static final NioClientSocketChannelFactory FACTORY = new NioClientSocketChannelFactory(
			Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

	//客户端启动器
	public static final ClientBootstrap bootstrap = new ClientBootstrap(FACTORY);

	//服务器地址
	public static final InetSocketAddress remoteServerAddress = new InetSocketAddress("127.0.0.1", 8080);
	
	private static final HashedWheelTimer timer = new HashedWheelTimer();
	
	private static ReadTimeoutHandler timeoutHandler = new ReadTimeoutHandler(timer,3);
	
	//客户端执行服务器返回response的处理链pipeline
	//这里是连接失败时导致大量TCP连接出现的原因,http://javatar.iteye.com/blog/1138527
	//由于外层调用采用单线程池,这里又将pipeline静态化,实验查看后并未出现TCP大量存在的情况
	private static final ChannelPipelineFactory CHANNEL_PIPELINE_FACTORY = new ChannelPipelineFactory() {
		
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			//pipeline 顺序调用,类似web的请求filter
			ChannelPipeline pipleline = pipeline();
			pipleline.addLast("encode", new StringEncoder());
			pipleline.addLast("decode", new StringDecoder());
			pipleline.addLast("timeout", timeoutHandler);// this is correct
//			pipleline.addLast("timeout", new ReadTimeoutHandler(new HashedWheelTimer(),3));//this is bug
			pipleline.addLast("handler", new ClientHanlder());//客户端handler
			return pipleline;
		}
	};
	
	static{
		bootstrap.setPipelineFactory(CHANNEL_PIPELINE_FACTORY);
	}
	
	//静态化ChannelFuture,因为客户端只需要保持一个连接跟服务器
	public static ChannelFuture channelFuture;
	
	//客户端线程标识名
	private String name;
	
	public ClientThread(String name){
		this.name = name;
	}

	@Override
	public void run() {
		if (!isShortTcp) {
			//与127.0.0.1建立长连接
			channelFuture = bootstrap.connect(remoteServerAddress);
			System.out.println("客户端 "+ name +" 启动完毕,返回长连接: " + channelFuture.hashCode());
		}
	}

	public String getName() {
		return name;
	}

}
