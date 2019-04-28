package com.serotonin.m2m2.tcp.server;

import java.nio.ByteOrder;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.tcp.handler.TCPConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TCPServer implements Runnable {
	EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
	EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Logger logger = LogManager.getLogger(TCPServer.class);
	private static final int LENGTH_FIELD_OFFSET = 19;
	private static final int LENGTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = 0;
	private static final int INITIAL_BYTES_TO_STRIP = 0;
	private static final int MAX_FRAME_LENGTH = 470000;// 最大允许的长度
	private ServerBootstrap tcpBootstrap = new ServerBootstrap();
	private TCPConfig tcpConfig;
	private List<DataPointRT> dataPointRTs;

	public TCPServer(TCPConfig tcpConfig, List<DataPointRT> dataPointRTs) {
		this.tcpConfig = tcpConfig;
		this.dataPointRTs = dataPointRTs;
	}

	public void run() {
		try {
			tcpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.option(ChannelOption.SO_KEEPALIVE, true).handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH,
									LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP,
									false));
							p.addLast(new TCPServerHandler(tcpConfig, dataPointRTs));
						}
					});

			tcpBootstrap.bind(tcpConfig.getServerPort()).sync();

		} catch (Exception e) {
			logger.error("connect tcp error", e);
		}
	}

	public void close() {
		if (!bossGroup.isShuttingDown()) {
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		try {

			EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			ServerBootstrap tcpBootstrap = new ServerBootstrap();
			tcpBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH,
									LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP,
									false));

							TCPConfig config = new TCPConfig(0, "1,2,3,4,5", "1,2,3,4,5");
							p.addLast(new TCPServerHandler(config, null));
						}
					});

			tcpBootstrap.bind(7100).sync();

		} catch (Exception e) {
			System.err.println("connect error");
		} 
	}
}
