package net.butfly.bus.gap.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.beykery.jkcp.Kcp;
import org.beykery.jkcp.KcpClient;
import org.beykery.jkcp.KcpOn;

public class TunnelClient extends KcpClient {
	@Override
	public void handleReceive(ByteBuf bb, KcpOn kcp) {
		String content = bb.toString(Charset.forName("utf-8"));
		System.out.println("conv:" + kcp.getKcp().getConv() + " recv:" + content + " kcp-->" + kcp);
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(2048);
		buf.writeBytes(content.getBytes(Charset.forName("utf-8")));
		kcp.send(buf);
		bb.release();
	}

	/**
	 * kcp异常，之后此kcp就会被关闭
	 *
	 * @param ex
	 * @param kcp
	 */
	@Override
	public void handleException(Throwable ex, KcpOn kcp) {
		System.out.println(ex);
	}

	@Override
	public void handleClose(KcpOn kcp) {
		super.handleClose(kcp);
		System.out.println("服务器离开:" + kcp);
		System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
	}

	@Override
	public void out(ByteBuf msg, Kcp kcp, Object user) {
		super.out(msg, kcp, user);
	}
}
