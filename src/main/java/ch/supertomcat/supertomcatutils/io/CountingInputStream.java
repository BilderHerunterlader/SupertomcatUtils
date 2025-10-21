package ch.supertomcat.supertomcatutils.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Counting Input Stream
 */
public class CountingInputStream extends FilterInputStream {
	/**
	 * Count of read bytes
	 */
	protected long count = 0;

	/**
	 * Constructor
	 * 
	 * @param in InputStream
	 */
	public CountingInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b != -1) {
			count++;
		}
		return b;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int n = super.read(b, off, len);
		if (n != -1) {
			count += n;
		}
		return n;
	}

	/**
	 * Returns the count
	 * 
	 * @return count
	 */
	public long getCount() {
		return count;
	}
}
