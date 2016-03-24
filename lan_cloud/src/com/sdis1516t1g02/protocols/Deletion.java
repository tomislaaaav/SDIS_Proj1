package com.sdis1516t1g02.protocols;

import com.sdis1516t1g02.Server;

/**
 * Created by Duarte on 19/03/2016.
 */
public class Deletion{

    String senderId;
    double version;
    String fileId;
    MessageType messageType;
    String args[];

    public Deletion(MessageType messageType, String versionStr, String senderId, String fileId, String[] args) {
        this.args = args;
        this.senderId = senderId;
        this.version = Double.valueOf(versionStr);
        this.fileId = fileId;
        this.messageType = messageType;
    }

    public void deleteChunk(){
        if(version >= 1.0)
            Server.getInstance().getChunckManager().deleteFile(this.fileId);
    }
}
