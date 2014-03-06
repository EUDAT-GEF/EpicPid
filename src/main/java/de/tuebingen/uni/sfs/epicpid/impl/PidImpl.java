package de.tuebingen.uni.sfs.epicpid.impl;

import de.tuebingen.uni.sfs.epicpid.Pid;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author edima
 */
public class PidImpl implements Pid {
	private final PidServerImpl pidServer;
	private final String hdl;
	private final URL url;

	public PidImpl(PidServerImpl pidServer, String hdl) throws IOException {
		this.pidServer = pidServer;
		this.hdl = hdl;
		this.url = new URL(Strings.CENTRAL_SERVER_PREFIX + hdl);
	}

	public URL getHandleSystemURL() {
		return url;
	}

	public String getId() {
		return hdl;
	}

	public void changeUrlTo(URI url) throws IOException {
		pidServer.changePidField(this, Strings.PID_RECORD_KEY_URL, url.toString());
	}

	public void changeChecksumTo(String checksum) throws IOException {
		pidServer.changePidField(this, Strings.PID_RECORD_KEY_CHECKSUM, checksum);
	}

	public void changeRoRTo(String RoR) throws IOException {
		pidServer.changePidField(this, Strings.PID_RECORD_KEY_ROR, RoR);
	}

	public String retrieveUrl() throws IOException {
		return pidServer.retrievePidFields(this).get(Strings.PID_RECORD_KEY_URL);
	}

	public String retrieveChecksum() throws IOException {
		return pidServer.retrievePidFields(this).get(Strings.PID_RECORD_KEY_CHECKSUM);
	}

	public String retrieveRoR() throws IOException {
		return pidServer.retrievePidFields(this).get(Strings.PID_RECORD_KEY_ROR);
	}
}
