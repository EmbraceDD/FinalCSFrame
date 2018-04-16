package com.lzy.csFrame.common;

public interface ICommunicationPublisher {
        void addCommunicationListenner(ICommunicationListener listener);
        void removeCommunicationListenner(ICommunicationListener listener);
}
