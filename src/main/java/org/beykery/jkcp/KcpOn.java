package org.beykery.jkcp;

import io.netty.buffer.ByteBuf;

public interface KcpOn {
	Kcp getKcp();

	void send(ByteBuf buf);

	void input(ByteBuf content);

	void noDelay(int nodelay, int interval, int resend, int nc);

	void wndSize(int sndwnd, int rcvwnd);

	void setMtu(int mtu);

	void setConv(int conv);

	void setMinRto(int minRto);

	void setStream(boolean stream);

	void setTimeout(long timeout);

	boolean isClosed();

	void update();

	void release();

	boolean needUpdate();
}
