package test.local;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import org.beykery.jkcp.KcpOn;
import org.beykery.jkcp.KcpServer;

public class JkcpLocalServer extends KcpServer {

	public JkcpLocalServer(int port, int workerSize) {
		super(port, workerSize);
	}

	@Override
	public void handleReceive(ByteBuf bb, KcpOn kcp) {
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

	/**
	 * 测试
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		JkcpLocalServer s = new JkcpLocalServer(2222, 1);
		s.noDelay(1, 10, 2, 1);
		s.setMinRto(10);
		s.wndSize(64, 64);
		s.setTimeout(10 * 1000);
		s.setMtu(512);
		s.start();
	}
}
