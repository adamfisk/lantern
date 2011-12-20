package org.lantern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DefaultConfigApiTest {

    @Test 
    public void testGlobalConfig() throws Exception {
        final ConfigApi conf = new DefaultConfigApi();
        final String json = conf.configAsJson();
        System.out.println(json);
        final JsonParser parser = new JsonParser();
        final JsonElement parsed = parser.parse(json);
        final JsonObject read = parsed.getAsJsonObject();
        final JsonObject system = (JsonObject) read.get("system");
        assertTrue(StringUtils.isNotBlank(system.toString()));
        
        final JsonElement port = system.get("port");
        assertEquals(LanternConstants.LANTERN_LOCALHOST_HTTP_PORT, port.getAsInt());
    }
    
    @Test 
    public void testWhitelist() throws Exception {
        final ConfigApi conf = new DefaultConfigApi();
        final String wl = conf.whitelist();
        
        final JsonParser parser = new JsonParser();
        final JsonElement parsed = parser.parse(wl);
        final JsonArray read = parsed.getAsJsonArray();
        boolean foundAvaaz = false;
        for (int i =0; i < read.size(); i++) {
            final JsonElement cur = read.get(i);
            if (cur.toString().contains("avaaz.org")) {
                foundAvaaz = true;
            }
        }
        
        //final JsonElement avaaz = read.get("avaaz.org");
        assertTrue(foundAvaaz);
    }
    
    @Test 
    public void testHttpsEverywhere() throws Exception {
        final ConfigApi conf = new DefaultConfigApi();
        final String json = conf.httpsEverywhere();
        final JsonParser parser = new JsonParser();
        final JsonElement parsed = parser.parse(json);
        final JsonObject read = parsed.getAsJsonObject();
        final JsonElement avaaz = read.get("avaaz.org");
        assertTrue(avaaz != null);
        final JsonElement rules = avaaz.getAsJsonObject().get("rules");
        assertTrue(rules != null);
    }
}