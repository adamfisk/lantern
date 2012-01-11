package org.lantern;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.lantern.SettingsState.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * This class is responsible for taking any actions required to serialize
 * settings to disk as well as to take actions based on settings changes.
 */
public class SettingsIo {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final File settingsFile;
    
    private final SettingsChangeImplementor implementor =
        new SettingsChangeImplementor();
    
    /**
     * Creates a new instance with all the default operations.
     */
    public SettingsIo() {
        this(LanternConstants.DEFAULT_SETTINGS_FILE);
    }
    
    
    /**
     * Creates a new instance with custom settings typically used only in 
     * testing.
     * 
     * @param settingsFile The file where settings are stored.
     */
    public SettingsIo(final File settingsFile) {
        this.settingsFile = settingsFile;
    }

    /**
     * Reads settings from disk.
     * 
     * @return The {@link Settings} instance as read from disk.
     */
    public Settings read() {
        if (!settingsFile.isFile()) {
            return newSettings();
        }
        final ObjectMapper mapper = new ObjectMapper();
        InputStream is = null;
        try {
            is = LanternUtils.localDecryptInputStream(settingsFile);
            final String json = IOUtils.toString(is);
            log.info("Building setting from json string...");
            final Settings read = mapper.readValue(json, Settings.class);
            log.info("Built settings from disk: {}", read);
            return read;
        } catch (final IOException e) {
            log.error("Could not read settings", e);
        } catch (final GeneralSecurityException e) {
            log.error("Could not read settings", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        settingsFile.delete();
        final Settings settings = newSettings();
        final SettingsState ss = new SettingsState();
        ss.setState(State.CORRUPTED);
        ss.setMessage("Could not read settings file.");
        return settings;
    }
    
    private Settings newSettings() {
        return new Settings(new Whitelist(), new Roster());
    }

    /**
     * Writes the default settings object.
     */
    public void write() {
        write(LanternHub.settings());
    }
    
    /**
     * Applies the given settings, including serializing them.
     * 
     * @param settings The settings to apply.
     */
    public void write(final Settings settings) {
        final Map<String, LanternPresence> entries = settings.getRoster().getEntries();
        //log.info("Writing:\n{}", json);
        OutputStream os = null;
        try {
            
            settings.getRoster().setEntries(new HashMap<String, LanternPresence>());
            final String json = LanternUtils.jsonify(settings);
            os = LanternUtils.localEncryptOutputStream(settingsFile);
            os.write(json.getBytes("UTF-8"));
        } catch (final IOException e) {
            log.error("Error encrypting stream", e);
        } catch (final GeneralSecurityException e) {
            log.error("Error encrypting stream", e);
        } finally {
            IOUtils.closeQuietly(os);
            settings.getRoster().setEntries(entries);
        }
        
    }
}