package com.lzy.csFrame.common;

public interface INetMessagePublisher {
	void	 addListener(INetMessageListener listener);
	void	 removeListener(INetMessageListener listener);
}
