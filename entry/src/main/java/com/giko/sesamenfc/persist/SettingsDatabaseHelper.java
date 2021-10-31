package com.giko.sesamenfc.persist;

import com.giko.sesamenfc.cmd.QRCodeInfo;
import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;

import java.io.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Settings database helper, persist the user config
 */
public class SettingsDatabaseHelper extends DatabaseHelper {
    private static final String CONFIGURATION_FILE_NAME = "settings";
    private static final String SETTING_QR_INFO = "QRInfoSet";
    private static final String SETTING_API_KEY = "APIKey";

    private static volatile SettingsDatabaseHelper databaseHelper;

    private SettingsDatabaseHelper(Context context) {
        super(context);
    }

    public static SettingsDatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            synchronized (SettingsDatabaseHelper.class) {
                if (databaseHelper == null) {
                    databaseHelper = new SettingsDatabaseHelper(context);
                }
            }
        }
        return databaseHelper;
    }

    public Preferences getPreferences() {
        return getPreferences(CONFIGURATION_FILE_NAME);
    }

    public void addQRCodeInfo(QRCodeInfo info){
        try {
            Preferences preferences = this.getPreferences();
            preferences.putString(SETTING_QR_INFO, toString(info));
            preferences.flushSync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAPIKey() {
        return this.getPreferences().getString(SETTING_API_KEY, null);
    }

    public void setAPIKey(String apiKey) {
        this.getPreferences().putString(SETTING_API_KEY, apiKey);
        this.getPreferences().flushSync();
    }

    public QRCodeInfo getQRCodeInfoSet() {
        Preferences preferences = this.getPreferences();
        String info = preferences.getString(SETTING_QR_INFO, null);
        if (info == null || info.length() == 0) {
            return null;
        }
        try {
            return fromString(info);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Write the object to a Base64 string. */
    private static String toString(QRCodeInfo o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Read the object from Base64 string. */
    private static QRCodeInfo fromString(String s) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        QRCodeInfo o  = (QRCodeInfo)ois.readObject();
        ois.close();
        return o;
    }

}
