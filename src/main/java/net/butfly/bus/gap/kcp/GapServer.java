package net.butfly.bus.gap.kcp;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;

public class GapServer {
	private TunnelClient client;
	private TunnelServer server;

	public GapServer(int port, int works) {
		super();
		server = new TunnelServer(port, works);
		server.noDelay(1, 10, 2, 1);
		server.setMinRto(10);
		server.wndSize(64, 64);
		server.setTimeout(10 * 1000);
		server.setMtu(512);
		server.start();

		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		client = new TunnelClient();
		TunnelClient tc = new TunnelClient();
		tc.noDelay(1, 20, 2, 1);
		tc.setMinRto(10);
		tc.wndSize(32, 32);
		tc.setTimeout(10 * 1000);
		tc.setMtu(512);
		// tc.setConv(121106);//默认conv随机

		tc.connect(new InetSocketAddress(, port));
		tc.start();
	}
}
