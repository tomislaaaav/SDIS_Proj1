package com.sdis1516t1g02.chunks;

import com.sdis1516t1g02.Server;

import java.io.*;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Duarte on 21/03/2016.
 */
public class ChunkManager {
    public final static String FOLDER_PATH = "chuncks/";
    public final static String CHUNK_EXTENSION=".chunck";

    Hashtable<String,BackupFile> files;

    public ChunkManager(){
        files = new Hashtable<>();
    }

    public static String generateFilename(String fileId, int chunkNo){
        return fileId+"_"+chunkNo;
    }

    public boolean addChunk(String fileId, int chunkNo, byte[] data, int replicationDegree) throws ChunkException {
        if(!Server.getInstance().hasSpaceForChunk())
            throw new ChunkException("Not enough space for a new chunk. Available space: "+Server.getByteCount(Server.getInstance().getAvailableSpace(),true));
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            backupFile = new BackupFile(fileId);
            files.put(fileId, backupFile);
        }

        Chunk chunk = getNotStoredChunk(fileId, chunkNo, replicationDegree, backupFile);
        return writeChunk(data, chunk);
    }

    public byte[] getChunkData(String fileId, int chunkNo) throws ChunkException {
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            throw new ChunkException("No information about file with fileId="+fileId);
        }
        Chunk chunk = backupFile.chunks.get(chunkNo);
        if(chunk == null || chunk.getState() != Chunk.State.STORED)
            throw new ChunkException("fileId= "+ fileId +" chunkNo="+chunkNo+" not stored");
        return readChunk(chunk);
    }

    public boolean deleteFile(String fileId){
        BackupFile backupFile = files.get(fileId);
        if(backupFile == null){
            return false;
        }
        Set<Integer> keySet = backupFile.chunks.keySet();
        for(Integer key : keySet){
            Chunk chunk = backupFile.chunks.get(key);
            if(deleteChunk(chunk)) {
                chunk.setState(Chunk.State.REMOVED);
                chunk.networkCopies = 0;
            }
        }
        return true;
    }

    protected Chunk getNotStoredChunk(String fileId, int chunkNo, int replicationDegree, BackupFile backupFile) throws ChunkException {
        Chunk chunk;
        chunk = backupFile.chunks.get(new Integer(chunkNo));
        if(chunk != null) {
            synchronized (chunk.state) {
                if (chunk.getState() == Chunk.State.STORED)
                    throw new ChunkException("Chunk is already stored");

            }
        }else{
            chunk = new Chunk(chunkNo,generateFilename(fileId,chunkNo), replicationDegree);
        }
        chunk.setState(Chunk.State.STORED);
        return chunk;
    }

    protected boolean writeChunk(byte[] data, Chunk chunk) throws ChunkException {
        File chunkFile = new File(FOLDER_PATH+chunk.filename+CHUNK_EXTENSION);
        try {
            if(!chunkFile.createNewFile())
                throw new ChunkException("Chunk file was already stored but there was no record");
            FileOutputStream out = new FileOutputStream(chunkFile);
            try {
                java.nio.channels.FileLock lock = out.getChannel().lock();
                try {
                    Writer writer = new OutputStreamWriter(out);
                    writer.write(new String(data));

                } finally {
                    lock.release();
                }
            } finally {
                out.close();
                chunk.incNetworkCopy();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected byte[] readChunk(Chunk chunk) throws ChunkException {
        String path = FOLDER_PATH+chunk.filename+CHUNK_EXTENSION;
        File chunkFile = new File(path);
        try {
            if(!chunkFile.exists())
                throw new ChunkException("Chunk file doesn't exist! Chunk-"+chunk.chunkNo +" Path-"+path);
            FileInputStream in = new FileInputStream(chunkFile);
            byte data[]= new byte[Server.CHUNK_SIZE];

            try {
                java.nio.channels.FileLock lock = in.getChannel().lock();
                try {
                    Reader reader = new InputStreamReader(in);
                    char cbuf[] = new char[Server.CHUNK_SIZE/2];
                    reader.read(cbuf);
                    String dataStr = new String(cbuf);
                    data = dataStr.getBytes();
                } finally {
                    lock.release();
                }
            } finally {
                in.close();
                return data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ChunkException("Unknown error reading from file!");
    }


    protected boolean deleteChunk(Chunk chunk) {
        Path path = Paths.get(FOLDER_PATH,chunk.filename,CHUNK_EXTENSION);
        int i;
        for (i = 0; i < 5 ; i++) {
            try{
                try {
                    Files.delete(path);
                } catch (NoSuchFileException x) {
                    System.err.format("%s: no such" + " file or directory%n", path);
                    throw x;
                } catch (DirectoryNotEmptyException x) {
                    System.err.format("%s not empty%n", path);
                    throw x;
                } catch (IOException x) {
                    // File permission problems are caught here.
                    System.err.println(x);
                    throw x;
                }
            }catch (Exception x){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            break;
        }
        if (i >= 5)
            return false;

        return true;
    }

    public Hashtable<String, BackupFile> getFiles() {
        return files;
    }
}
