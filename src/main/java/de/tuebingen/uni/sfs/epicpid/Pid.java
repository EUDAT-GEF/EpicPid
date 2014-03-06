package de.tuebingen.uni.sfs.epicpid;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author edima
 */
public interface Pid {
	URL getHandleSystemURL();

	String getId();

	String retrieveUrl() throws IOException;

	String retrieveChecksum() throws IOException;

    //** @returns record of reference, the original record */
	String retrieveRoR() throws IOException;

	void changeUrlTo(URI url) throws IOException;

	void changeChecksumTo(String checksum) throws IOException;

	void changeRoRTo(String ror) throws IOException;
}
