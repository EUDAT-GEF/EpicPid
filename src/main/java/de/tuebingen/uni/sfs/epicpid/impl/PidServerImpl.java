package de.tuebingen.uni.sfs.epicpid.impl;

import de.tuebingen.uni.sfs.epicpid.PidServer;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import de.tuebingen.uni.sfs.epicpid.Pid;
import de.tuebingen.uni.sfs.epicpid.PidServerConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author edima
 */
public class PidServerImpl implements PidServer {

    private String epicServer;
    private String localPrefix;
    private final Client client;
    private final WebResource wr;

    public PidServerImpl(PidServerConfig psc) throws NoSuchAlgorithmException {
        if (psc.epicServerUrl == null)
            throw new IllegalArgumentException("configuration parameter `epicServerUrl` is null");
        if (psc.localPrefix == null)
            throw new IllegalArgumentException("configuration parameter `localPrefix` is null");
        if (psc.username == null)
            throw new IllegalArgumentException("configuration parameter `username` is null");
        if (psc.password == null)
            throw new IllegalArgumentException("configuration parameter `password` is null");
        
        epicServer = psc.epicServerUrl;
        localPrefix = psc.localPrefix;

        ClientConfig config = new DefaultClientConfig();
//		SSLContext ctx = SSLContext.getInstance("SSL");
//		ctx.init(null, myTrustManager, null);
//		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hostnameVerifier, ctx));
        if (!epicServer.endsWith("/")) {
            epicServer += "/";
        }
        if (!localPrefix.endsWith("/")) {
            localPrefix += "/";
        }
        client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(psc.username, psc.password));
        wr = client.resource(epicServer);
    }

    static class PidData {

        String type;
        String parsed_data;

        public PidData(String type, String parsedData) {
            this.type = type;
            this.parsed_data = parsedData;
        }
    }

    @Override
    public Pid makePid(URL url, String checksum, String RoR) throws IOException {
        return makePid(url.toExternalForm(), checksum, RoR);
    }

    @Override
    public Pid makePid(String uri, String checksum, String RoR) throws IOException {
        try {
            return tryMakePid(uri, checksum, RoR);
        } catch (Exception xc) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
            return tryMakePid(uri, checksum, RoR);
        }
    }

    private Pid tryMakePid(String uri, String checksum, String RoR) throws IOException {
        List<PidData> data = new ArrayList<PidData>();
        data.add(new PidData(Strings.PID_RECORD_KEY_URL, uri));
        if (checksum != null) {
            data.add(new PidData(Strings.PID_RECORD_KEY_CHECKSUM, checksum));
        }
        if (RoR != null) {
            data.add(new PidData(Strings.PID_RECORD_KEY_ROR, RoR));
        }

        Gson gson = new Gson();
        String sdata = gson.toJson(data);

        ClientResponse cr = wr.path(localPrefix)
                .header(Strings.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sdata);

        if (cr.getStatus() / 100 != 2) {
            throw new IOException("error when creating PID: server returned: " + cr.getStatus());
        }
        if (cr.getLocation() == null) {
            throw new IOException("error when creating PID: location is null");
        }
        //System.out.println("pid returned: " + cr.getLocation());

        String hdl = cr.getLocation().toString();
        if (hdl.startsWith(epicServer)) {
            hdl = hdl.substring(epicServer.length());
        }
        return new PidImpl(this, hdl);
    }

    public Pid fromString(String pid) throws IOException {
        if (pid.startsWith(Strings.CENTRAL_SERVER_PREFIX)) {
            pid = pid.substring(Strings.CENTRAL_SERVER_PREFIX.length());
        } else if (pid.startsWith(epicServer)) {
            pid = pid.substring(epicServer.length());
        }

        return new PidImpl(this, pid);
    }

    void changePidField(Pid pid, String fieldName, String content) throws IOException {
        List<Map> data = retrievePidData(pid);
        changePidRecord(data, fieldName, content);
        String sdata = new Gson().toJson(data);

        ClientResponse cr = wr.path(pid.getId())
                .header(Strings.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, sdata);

        if (cr.getStatus() / 100 != 2) {
            throw new IOException("error when changing PID field " + fieldName
                    + ": server returned: " + cr.getStatus() + ": " + cr.getEntity(String.class));
        }
    }

    void changePidRecord(List<Map> data, String fieldName, String content) {
        for (Map m : data) {
            if (m.get(Strings.PID_RECORD_JSON_KEY) != null
                    && m.get(Strings.PID_RECORD_JSON_KEY).equals(fieldName)) {
                if (content == null) {
                    data.remove(m);
                    return;
                } else {
                    m.put(Strings.PID_RECORD_JSON_DATA, content);
                    m.remove(Strings.PID_RECORD_JSON_HEX_DATA);
                    return;
                }
            }
        }
        // if we reached this line, add new field
        if (content != null) {
            Map m = new HashMap();
            m.put(Strings.PID_RECORD_JSON_KEY, fieldName);
            m.put(Strings.PID_RECORD_JSON_DATA, content);
            data.add(m);
        }
    }

    List<Map> retrievePidData(Pid pid) {
        String json = wr.path(pid.getId())
                .header(Strings.HEADER_ACCEPT, MediaType.APPLICATION_JSON)
                .get(String.class);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
        Gson gson = gsonBuilder.create();

        return (List<Map>) gson.fromJson(json, Object.class);
    }

    Map<String, String> retrievePidFields(Pid pid) {
        Map<String, String> pidData = new HashMap<String, String>();

        List<Map> jslist = retrievePidData(pid);
        for (Map jsmap : jslist) {
            if (jsmap.containsKey(Strings.PID_RECORD_JSON_KEY)) {
                pidData.put("" + jsmap.get(Strings.PID_RECORD_JSON_KEY), "" + jsmap.get(Strings.PID_RECORD_JSON_DATA));
            }
        }

        return pidData;
    }

    public Pid searchByURI(String uri) throws IOException {
        ClientResponse cr = wr.path(localPrefix)
                .queryParam(Strings.PID_RECORD_KEY_URL, uri.toString())
                .header(Strings.HEADER_ACCEPT, MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        if (cr.getStatus() / 100 != 2) {
            throw new IOException("error when searching for URL: server returned: " + cr.getStatus());
        }

        Gson gson = new Gson();
        String json = cr.getEntity(String.class);
        if (json == null) {
            throw new IOException("null json");
        }
        if (json.isEmpty()) {
            throw new IOException("empty json");
        }

        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> jlist = gson.fromJson(json, listType);
        if (jlist == null) {
            throw new IOException("null json list");
        }
        if (jlist.isEmpty()) {
            throw new IOException("empty json list");
        }

        String hdl = jlist.get(0);
        if (hdl.startsWith(epicServer)) {
            hdl = hdl.substring(epicServer.length());
        } else if (hdl.startsWith(localPrefix)) {
            // fine
        } else {
            hdl = localPrefix + hdl;
        }
        return new PidImpl(this, hdl);
    }

    // http://stackoverflow.com/questions/2779251/convert-json-to-hashmap-using-gson-in-java
    // thanks Kevin Dolan
    private static class NaturalDeserializer implements JsonDeserializer<Object> {

        public Object deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) {
            if (json.isJsonNull()) {
                return null;
            } else if (json.isJsonPrimitive()) {
                return handlePrimitive(json.getAsJsonPrimitive());
            } else if (json.isJsonArray()) {
                return handleArray(json.getAsJsonArray(), context);
            } else {
                return handleObject(json.getAsJsonObject(), context);
            }
        }

        private Object handlePrimitive(JsonPrimitive json) {
            if (json.isBoolean()) {
                return json.getAsBoolean();
            } else if (json.isString()) {
                return json.getAsString();
            } else {
                BigDecimal bigDec = json.getAsBigDecimal();
                // Find out if it is an int type
                try {
                    bigDec.toBigIntegerExact();
                    try {
                        return bigDec.intValueExact();
                    } catch (ArithmeticException e) {
                    }
                    return bigDec.longValue();
                } catch (ArithmeticException e) {
                }
                // Just return it as a double
                return bigDec.doubleValue();
            }
        }

        private Object handleArray(JsonArray json, JsonDeserializationContext context) {
            List<Object> list = new ArrayList<Object>();
            for (JsonElement je : json) {
                list.add(context.deserialize(je, Object.class));
            }
            return list;
        }

        private Object handleObject(JsonObject json, JsonDeserializationContext context) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                map.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
            }
            return map;
        }
    }
}
