package com.spreadit.network;

public interface AsyncResponse {
    void processReqServIdFinish(String output);
    void processSendMessageFinish();
    void processSendLocationFinish();
    void processSendResetTTLFinish();
    void processSendLogoutFinish();
    void processGetSurroundingUsersFinish(String response);
	void processSendResetDatabaseFinish();
}
