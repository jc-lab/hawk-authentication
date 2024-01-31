package kr.jclab.hawk.spring2;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public class ContentPreCachingRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;
    private CachedInputStream cachedInputStream;

    public ContentPreCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = IOUtils.toByteArray(request.getInputStream());
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.cachedInputStream == null) {
            this.cachedInputStream = new CachedInputStream(getRequest().getInputStream(), body);
        }
        return this.cachedInputStream;
    }

    static class CachedInputStream extends ServletInputStream {
        private final ServletInputStream is;
        private ReadListener listener;

        private final byte[] buf;
        private int pos;
        private int mark = 0;
        private int count;

        public CachedInputStream(ServletInputStream is, byte[] body) {
            this.is = is;
            this.buf = body;
            this.pos = 0;
            this.count = buf.length;
        }

        @Override
        public synchronized int read() throws IOException {
            int c = this.pos < this.count ? this.buf[this.pos++] & 255 : -1;
            if (this.pos == this.count) {
                finished();
            }
            return c;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, b.length);
            if (this.pos >= this.count) {
                return -1;
            } else {
                int avail = this.count - this.pos;
                if (len > avail) {
                    len = avail;
                }

                if (len <= 0) {
                    return 0;
                } else {
                    System.arraycopy(this.buf, this.pos, b, off, len);
                    this.pos += len;
                    if (this.pos == this.count) {
                        finished();
                    }
                    return len;
                }
            }
        }

        @Override
        public synchronized byte[] readAllBytes() {
            byte[] result = Arrays.copyOfRange(this.buf, this.pos, this.count);
            this.pos = this.count;
            return result;
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            int n = this.read(b, off, len);
            return n == -1 ? 0 : n;
        }

        @Override
        public synchronized long transferTo(OutputStream out) throws IOException {
            int len = this.count - this.pos;
            out.write(this.buf, this.pos, len);
            this.pos = this.count;
            return (long)len;
        }

        @Override
        public synchronized long skip(long n) throws IOException {
            long k = (long)(this.count - this.pos);
            if (n < k) {
                k = n < 0L ? 0L : n;
            }

            this.pos = (int)((long)this.pos + k);

            if (this.pos == this.count) {
                finished();
            }

            return k;
        }

        @Override
        public synchronized int available() {
            return this.count - this.pos;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readAheadLimit) {
            this.mark = this.pos;
        }

        @Override
        public synchronized void reset() {
            this.pos = this.mark;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public boolean isFinished() {
            return available() == 0;
        }

        @Override
        public boolean isReady() {
            return is.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            this.listener = listener;
            try {
                listener.onDataAvailable();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void finished() throws IOException {
            if (listener != null) {
                listener.onAllDataRead();
            }
        }
    }
}
