package net.sf.sageplugins.webserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class TranscodeInputStream extends InputStream {
    private InputStream mIs;
    private Process mProcess;
    private int mPort;
    private boolean mClosed;
    private String mFileExt;

    public TranscodeInputStream(InputStream is, Process process, int port, String fileExt) {
        this(is, process, port, fileExt, -1);
    }

    public TranscodeInputStream(InputStream is, Process process, int port, String fileExt, int bufferSize) {
        if (bufferSize < 0)
        {
            mIs = new BufferedInputStream(is);
        }
        else
        {
            mIs = new BufferedInputStream(is, bufferSize);
        }
        mProcess = process;
        mPort = port;
        mClosed = false;
        mFileExt = fileExt;
    }

    public int available() throws IOException {
        return mIs.available();
    }

    public int read() throws IOException {
        return mIs.read();
    }

    public void close() throws IOException {
        mIs.close();
        System.out.println("Shutting down vlc transcoder process");
        VlcTranscodeMgr.getInstance().stopTranscodeProcess(this);
        mClosed = true;
    }

    public synchronized void reset() throws IOException {
        mIs.reset();
    }

    public boolean markSupported() {
        return mIs.markSupported();
    }

    public synchronized void mark(int readlimit) {
        mIs.mark(readlimit);
    }

    public long skip(long n) throws IOException {
        return mIs.skip(n);
    }

    public int read(byte b[]) throws IOException {
        return mIs.read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return mIs.read(b, off, len);
    }

    public Process getProcess() {
        return mProcess;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isClosed() {
        return mClosed;
    }

    public String getFileExt() {
        return mFileExt;
    }
}
