package de.tuebingen.uni.sfs.epicpid.repo;

import de.tuebingen.uni.sfs.epicpid.Pid;
import de.tuebingen.uni.sfs.epicpid.PidServer;
import java.io.IOException;

/**
 * @author edima
 */
public interface RepositoryPidServer extends PidServer {
	static final String CLARIN_RESOLVER = "http://weblicht.sfs.uni-tuebingen.de/PidResolver/clarin/";
	static final String SFB833_RESOLVER = "http://weblicht.sfs.uni-tuebingen.de/PidResolver/sfb833/";

	public static final String FEDORA_CLARIN_URL = "https://weblicht.sfs.uni-tuebingen.de/fedora/describe";
	public static final String FEDORA_CLARIN_PID = "http://hdl.handle.net/11022/0000-0000-1B7F-5";
	
	public static final String FEDORA_SFB833_URL = "https://repository.sfb833.uni-tuebingen.de:8443/fedora/describe";
	public static final String FEDORA_SFB833_PID = "http://hdl.handle.net/11022/0000-0000-1BEA-B";

	public Pid makeClarinRepositoryPid(String doId, String checksum) throws IOException;

	public Pid makeSfb833RepositoryPid(String doId, String checksum) throws IOException;
}
