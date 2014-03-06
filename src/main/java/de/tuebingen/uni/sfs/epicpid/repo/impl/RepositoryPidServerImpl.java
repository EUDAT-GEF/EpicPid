package de.tuebingen.uni.sfs.epicpid.repo.impl;

import de.tuebingen.uni.sfs.epicpid.repo.RepositoryPidServer;
import de.tuebingen.uni.sfs.epicpid.*;
import de.tuebingen.uni.sfs.epicpid.impl.PidServerImpl;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author edima
 */
public class RepositoryPidServerImpl extends PidServerImpl implements RepositoryPidServer {
	public RepositoryPidServerImpl(PidServerConfig config) throws NoSuchAlgorithmException {
		super(config);
	}

	public Pid makeClarinRepositoryPid(String doId, String checksum) throws IOException {
		return makePid(CLARIN_RESOLVER + doId, checksum, null);
	}

	public Pid makeSfb833RepositoryPid(String doId, String checksum) throws IOException {
		return makePid(SFB833_RESOLVER + doId, checksum, null);
	}
}
