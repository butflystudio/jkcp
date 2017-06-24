/**
 *
 */
package org.beykery.jkcp;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author beykery
 */
public interface KcpListerner {

	/**
	 * kcp message
	 *
	 * @param bb
	 *            the data
	 * @param kcp
	 */
	public void handleReceive(ByteBuf bb, KcpOn kcp);

	/**
	 *
	 * kcp异常，之后此kcp就会被关闭
	 *
	 * @param ex
	 *            异常
	 * @param kcp
	 *            发生异常的kcp，null表示非kcp错误
	 */
	public void handleException(Throwable ex, KcpOn kcp);

	/**
	 * 关闭
	 *
	 * @param kcp
	 */
	public void handleClose(KcpOn kcp);
}
