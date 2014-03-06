package de.tuebingen.uni.sfs.epicpid;

import java.io.IOException;
import java.net.URL;

/**
 *
 * @author edima
 */
public interface PidServer {

    Pid makePid(URL url, String checksum, String RoR) throws IOException;

    Pid makePid(String url, String checksum, String RoR) throws IOException;

    Pid searchByURI(String uri) throws IOException;

    Pid fromString(String workflowPid) throws IOException;
}
