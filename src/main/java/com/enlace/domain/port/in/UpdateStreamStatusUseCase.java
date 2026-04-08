package com.enlace.domain.port.in;
 
public interface UpdateStreamStatusUseCase {
    void update(String channelName, String eventName, String streamId, String recordingS3KeyPrefix);
}
