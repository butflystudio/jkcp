package net.butfly.bus.gap.kcp;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import org.beykery.jkcp.KcpOn;
import org.beykery.jkcp.KcpServer;

public class TunnelServer extends KcpServer {
	public TunnelServer(int port, int workerSize) {
		super(port, workerSize);
	}

	@Override
	public void handleReceive(ByteBuf bb, KcpOn kcp) {
		byte flag = bb.readByte();
		switch (flag) {
		case 1:
			break;
		case 0: // control: tell server the client listen port
			int port = bb.readInt();
			TunnelSession.re
			break;
		}
		String content = bb.toString(Charset.forName("utf-8"));
		System.out.println("msg:" + content + " kcp--> " + kcp);
		kcp.send(bb);// echo
	}

	@Override
	public void handleException(Throwable ex, KcpOn kcp) {
		System.out.println(ex);
	}

	@Override
	public void handleClose(KcpOn kcp) {
		System.out.println("客户端离开:" + kcp);
		System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
	}
}
