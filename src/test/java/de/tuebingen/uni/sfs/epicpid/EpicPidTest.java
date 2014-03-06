package de.tuebingen.uni.sfs.epicpid;

import de.tuebingen.uni.sfs.epicpid.impl.PidServerImpl;
import de.tuebingen.uni.sfs.epicpid.repo.RepositoryPidServer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author edima
 */
@Ignore // we cannot build with unittests, unless we specify a test server configuration below
public class EpicPidTest {
    static PidServerConfig psc = new PidServerConfig() {
        {
            localPrefix = "";
            username = "";
            password = "";
        }
    };

    @Test
    public void testMakeSimplePid() throws NoSuchAlgorithmException, IOException {
        PidServerImpl pider = new PidServerImpl(psc);

        URL url = new URL("http://test.case/");
        String pid = pider.makePid(url, null, null).getId();

        System.out.println("new simple pid: " + pid);
        Assert.assertTrue(pid.matches("\\d+/([0-9A-Fa-f]+-)*[0-9A-Fa-f]+"));
    }

    @Test
    public void testMakePid() throws NoSuchAlgorithmException, IOException {
        PidServerImpl pider = new PidServerImpl(psc);

        URL url = new URL("http://test.case/");
        String pid = pider.makePid(url, "2e457f62cccb3c5d2d724e0b5eb5f593", "11022/1002-clarin-ror").getId();

        System.out.println("new complex pid: " + pid);
        Assert.assertTrue(pid.matches("\\d+/([0-9A-Fa-f]+-)*[0-9A-Fa-f]+"));
    }

    @Test
    public void testSearchFedoraPid() throws NoSuchAlgorithmException, IOException {
        PidServerImpl pider = new PidServerImpl(psc);
        String pid = pider.searchByURI(RepositoryPidServer.FEDORA_CLARIN_URL).getHandleSystemURL().toExternalForm();
        Assert.assertTrue(pid.equals(RepositoryPidServer.FEDORA_CLARIN_PID));
    }

    @Test
    public void testComplex() throws NoSuchAlgorithmException, IOException, URISyntaxException {
        PidServerImpl pider = new PidServerImpl(psc);

        URL url = new URL("http://test.case/");
        String checksum = "2e457f62cccb3c5d2d724e0b5eb5f593";
        String ror = "11022/1002-clarin-ror";
        Pid pid = pider.makePid(url, checksum, ror);
        Assert.assertTrue(pid.getId().matches("\\d+/([0-9A-Fa-f]+-)*[0-9A-Fa-f]+"));

        System.out.println("pid: " + pid.getId() + " => " + pid.retrieveUrl() + "; " + pid.retrieveChecksum() + "; " + pid.retrieveRoR());

        Assert.assertEquals(pid.retrieveUrl(), url.toExternalForm());
        Assert.assertEquals(pid.retrieveChecksum(), checksum);
        Assert.assertEquals(pid.retrieveRoR(), ror);

        URI url2 = new URI("http://----.____/");
        pid.changeUrlTo(url2);
        System.out.println("pid: " + pid.getId() + " => " + pid.retrieveUrl() + "; " + pid.retrieveChecksum() + "; " + pid.retrieveRoR());

        Assert.assertEquals(url2.toString(), pid.retrieveUrl());
        Assert.assertEquals(checksum, pid.retrieveChecksum());
        Assert.assertEquals(ror, pid.retrieveRoR());

        String checksum2 = "11111111111111111111111111111111";
        pid.changeChecksumTo(checksum2);
        System.out.println("pid: " + pid.getId() + " => " + pid.retrieveUrl() + "; " + pid.retrieveChecksum() + "; " + pid.retrieveRoR());

        Assert.assertEquals(pid.retrieveUrl(), url2.toString());
        Assert.assertEquals(pid.retrieveChecksum(), checksum2);
        Assert.assertEquals(pid.retrieveRoR(), ror);
    }
}
