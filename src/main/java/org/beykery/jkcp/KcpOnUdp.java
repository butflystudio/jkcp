/**
 * udp for kcp
 */
package org.beykery.jkcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author beykery
 */
public class KcpOnUdp implements KcpOn {

	private final Kcp kcp;// kcp的状态
	private final Queue<ByteBuf> received;// 输入
	private final Queue<ByteBuf> sendList;
	private long timeout;// 超时设定
	private long lastTime;// 上次超时检查时间
	private int errcode;// 错误代码
	private final KcpListerner listerner;
	private volatile boolean needUpdate;
	private volatile boolean closed;
	private String sessionId;
	private final Map<Object, Object> session;
	private final InetSocketAddress remote;// 远程地址
	private final InetSocketAddress local;// 本地

	/**
	 * fastest: ikcp_nodelay(kcp, 1, 20, 2, 1) nodelay: 0:disable(default), 1:enable
	 * interval: internal update timer interval in millisec, default is 100ms
	 * resend: 0:disable fast resend(default), 1:enable fast resend nc: 0:normal
	 * congestion control(default), 1:disable congestion control
	 *
	 * @param nodelay
	 * @param interval
	 * @param resend
	 * @param nc
	 */
	@Override
	public void noDelay(int nodelay, int interval, int resend, int nc) {
		this.kcp.noDelay(nodelay, interval, resend, nc);
	}

	/**
	 * set maximum window size: sndwnd=32, rcvwnd=32 by default
	 *
	 * @param sndwnd
	 * @param rcvwnd
	 */
	@Override
	public void wndSize(int sndwnd, int rcvwnd) {
		this.kcp.wndSize(sndwnd, rcvwnd);
	}

	/**
	 * change MTU size, default is 1400
	 *
	 * @param mtu
	 */
	@Override
	public void setMtu(int mtu) {
		this.kcp.setMtu(mtu);
	}

	/**
	 * conv
	 *
	 * @param conv
	 */

	@Override
	public void setConv(int conv) {
		this.kcp.setConv(conv);
	}

	/**
	 * stream模式
	 *
	 * @param stream
	 */

	@Override
	public void setStream(boolean stream) {
		this.kcp.setStream(stream);
	}

	/**
	 * 流模式
	 *
	 * @return
	 */
	public boolean isStream() {
		return this.kcp.isStream();
	}

	/**
	 * rto设置
	 *
	 * @param rto
	 */
	@Override
	public void setMinRto(int rto) {
		this.kcp.setMinRto(rto);
	}

	/**
	 * kcp for udp
	 *
	 * @param out
	 *            输出接口
	 * @param remote
	 *            远程地址
	 * @param local
	 *            本地地址
	 * @param listerner
	 *            监听
	 */
	public KcpOnUdp(Output out, InetSocketAddress remote, InetSocketAddress local, KcpListerner listerner) {
		this.listerner = listerner;
		kcp = new Kcp(out, remote);
		received = new LinkedBlockingQueue<>();
		sendList = new LinkedBlockingQueue<>();
		this.session = new HashMap<>();
		this.remote = remote;
		this.local = local;
	}

	/**
	 * send data to addr
	 *
	 * @param bb
	 */
	@Override
	public void send(ByteBuf bb) {
		if (!closed) {
			this.sendList.add(bb);
			this.needUpdate = true;
		}
	}

	/**
	 * update one kcp
	 *
	 * @param addr
	 * @param kcp
	 */
	@Override
	public void update() {
		// input
		while (!this.received.isEmpty()) {
			ByteBuf dp = this.received.remove();
			errcode = kcp.input(dp);
			dp.release();
			if (errcode != 0) {
				this.closed = true;
				this.release();
				this.listerner.handleException(new IllegalStateException("input error : " + errcode), this);
				this.listerner.handleClose(this);
				return;
			}
		}
		// receive
		int len;
		while ((len = kcp.peekSize()) > 0) {
			ByteBuf bb = PooledByteBufAllocator.DEFAULT.buffer(len);
			int n = kcp.receive(bb);
			if (n > 0) {
				this.listerner.handleReceive(bb, this);
				this.lastTime = System.currentTimeMillis();
			} else {
				bb.release();
			}
		}
		// send
		while (!this.sendList.isEmpty()) {
			ByteBuf bb = sendList.remove();
			errcode = this.kcp.send(bb);
			if (errcode != 0) {
				this.closed = true;
				this.release();
				this.listerner.handleException(new IllegalStateException("send error : " + errcode), this);
				this.listerner.handleClose(this);
				return;
			}
		}
		// update kcp status
		int cur = (int) System.currentTimeMillis();
		if (this.needUpdate || cur >= kcp.getNextUpdate()) {
			kcp.update(cur);
			kcp.setNextUpdate(kcp.check(cur));
			this.needUpdate = false;
		}
		// check timeout
		if (this.timeout > 0 && lastTime > 0 && System.currentTimeMillis() - this.lastTime > this.timeout) {
			this.closed = true;
			this.release();
			this.listerner.handleClose(this);
		}
	}

	/**
	 * 输入
	 *
	 * @param content
	 */
	@Override
	public void input(ByteBuf content) {
		if (!this.closed) {
			this.received.add(content);
			this.needUpdate = true;
		} else {
			content.release();
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Kcp getKcp() {
		return kcp;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	@Override
	public String toString() {
		return "local: " + local + " remote: " + remote;
	}

	/**
	 * session id
	 *
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * session id
	 *
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * session map
	 *
	 * @return
	 */
	public Map<Object, Object> getSessionMap() {
		return session;
	}

	/**
	 * session k v
	 *
	 * @param k
	 * @return
	 */
	public Object getSession(Object k) {
		return this.session.get(k);
	}

	/**
	 * session k v
	 *
	 * @param k
	 * @param v
	 * @return
	 */
	public Object setSession(Object k, Object v) {
		return this.session.put(k, v);
	}

	/**
	 * contains key
	 *
	 * @param k
	 * @return
	 */
	public boolean containsSessionKey(Object k) {
		return this.session.containsKey(k);
	}

	/**
	 * contains value
	 *
	 * @param v
	 * @return
	 */
	public boolean containsSessionValue(Object v) {
		return this.session.containsValue(v);
	}

	/**
	 * 立即更新？
	 *
	 * @return
	 */
	@Override
	public
	boolean needUpdate() {
		return this.needUpdate;
	}

	/**
	 * 监听器
	 *
	 * @return
	 */
	public KcpListerner getListerner() {
		return listerner;
	}

	/**
	 * 本地地址
	 *
	 * @return
	 */
	public InetSocketAddress getLocal() {
		return local;
	}

	/**
	 * 远程地址
	 *
	 * @return
	 */
	public InetSocketAddress getRemote() {
		return remote;
	}

	/**
	 * 释放内存
	 */
	@Override
	public void release() {
		this.kcp.release();
		for (ByteBuf item : this.received) {
			item.release();
		}
		for (ByteBuf item : this.sendList) {
			item.release();
		}
	}

}
