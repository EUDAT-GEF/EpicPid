package de.tuebingen.uni.sfs.epicpid.mockimpl;

import de.tuebingen.uni.sfs.epicpid.Pid;
import de.tuebingen.uni.sfs.epicpid.PidServer;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

/**
 *
 * @author edima
 */
public class PidMockImpl implements PidServer {

	static final Random rand = new Random();
	final String prefix;
	final Map<String, PidImpl> pids = new HashMap<String, PidImpl>();

	public PidMockImpl() {
		long random = Math.abs(rand.nextInt(128 * 256) + 128 * 256);
		prefix = new BigInteger("" + random).toString(16);
	}

	public static class PidImpl implements Pid {

		String pidId; // 1234/5678-0987-6543
		String targetUrl;
		String checksum;
		String RoR;

		public PidImpl(String prefix, String url, String checksum, String RoR) {
			this.targetUrl = url;
			this.checksum = checksum;
			this.RoR = RoR;
			long random = Math.abs(rand.nextLong());
			String name = new BigInteger("" + random).toString(16);
			pidId = prefix + "/" + name;
		}

		@Override
		public URL getHandleSystemURL() {
			try {
				return new URL("hdl.handle.net/" + pidId);
			} catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public String getId() {
			return pidId;
		}

		@Override
		public String retrieveUrl() throws IOException {
			return targetUrl;
		}

		@Override
		public String retrieveChecksum() throws IOException {
			return checksum;
		}

		@Override
		public String retrieveRoR() throws IOException {
			return RoR;
		}

		@Override
		public void changeUrlTo(URI uri) throws IOException {
			targetUrl = uri.toString();
		}

		@Override
		public void changeChecksumTo(String checksum) throws IOException {
			this.checksum = checksum;
		}

		@Override
		public void changeRoRTo(String ror) throws IOException {
			RoR = ror;
		}
	}

	@Override
	public Pid makePid(URL url, String checksum, String RoR) throws IOException {
		return makePid(url.toExternalForm(), checksum, RoR);
	}

	@Override
	public Pid makePid(String url, String checksum, String RoR) throws IOException {
		PidImpl p = new PidImpl(prefix, url, checksum, RoR);
		while (pids.containsKey(p.pidId)) {
			p = new PidImpl(prefix, url, checksum, RoR);
		}
		pids.put(p.pidId, p);
		return p;
	}

	@Override
	public Pid searchByURI(final String uri) throws IOException {
		Predicate<PidImpl> endsWithUri = new Predicate<PidImpl>() {
			@Override
			public boolean test(PidImpl pid) {
				return pid.targetUrl.endsWith(uri);
			}
		};
		return pids
				.values()
				.stream()
				.filter(endsWithUri)
				.findFirst()
				.orElse(null);
	}

	@Override
	public Pid fromString(String pid) throws IOException {
		return pids.get(pid);
	}
}
