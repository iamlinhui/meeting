package cn.promptness.meeting.tool.utils;

import com.github.windpapi4j.WinDPAPI;
import com.github.windpapi4j.WinDPAPI.CryptProtectFlag;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ChromeDecryptHelper {

    public static Set<ChromeCookie> getWindowsDecryptedCookies(String sql) {
        String userHome = System.getProperties().getProperty("user.home");
        String cookieFileFullPathAndName = userHome + "/AppData/Local/Google/Chrome/User Data/Default/Cookies";
        String localStateFileFullPathAndName = userHome + "/AppData/Local/Google/Chrome/User Data/Local State";
        return new ChromeDecryptHelper(cookieFileFullPathAndName, localStateFileFullPathAndName).getDecryptedCookies(sql);
    }

    private static final int QUERY_TIMEOUT = 30;

    private static final String K_ENCRYPTION_VERSION_PREFIX = "v10";
    private static final String K_DPAPI_KEY_PREFIX = "DPAPI";

    private static final int KEY_LENGTH = 256 / 8;
    private static final int IV_LENGTH = 96 / 8;
    private static final int GCM_TAG_LENGTH = 16;

    private static final Logger logger = LoggerFactory.getLogger(ChromeDecryptHelper.class);

    private final String cookieFileFullPathAndName;
    private final String localStateFileFullPathAndName;


    private ChromeDecryptHelper(String cookieFileFullPathAndName, String localStateFileFullPathAndName) {
        this.cookieFileFullPathAndName = cookieFileFullPathAndName;
        this.localStateFileFullPathAndName = localStateFileFullPathAndName;
    }

    private Set<ChromeCookie> getDecryptedCookies(String sql) {
        HashSet<ChromeCookie> cookieSet = new HashSet<>();

        File cookieFile = new File(cookieFileFullPathAndName);
        if (!cookieFile.exists()) {
            return cookieSet;
        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + cookieFile.getAbsolutePath())) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setQueryTimeout(QUERY_TIMEOUT);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        parseCookieFromResult(cookieSet, resultSet);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.toString(), ex.fillInStackTrace());
        }
        return cookieSet;
    }

    private void parseCookieFromResult(HashSet<ChromeCookie> cookieSet, ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        byte[] encryptedBytes = resultSet.getBytes("encrypted_value");
        String path = resultSet.getString("path");
        String domain = resultSet.getString("host_key");
        boolean secure = resultSet.getBoolean("is_secure");
        boolean httpOnly = resultSet.getBoolean("is_httponly");
        Date expires = resultSet.getDate("expires_utc");

        byte[] decryptBytes = decryptBytes(encryptedBytes);
        if (decryptBytes != null) {
            ChromeCookie chromeCookie = ChromeCookie.builder().name(name).path(path).domain(domain).secure(secure).httpOnly(httpOnly).expires(expires).value(new String(decryptBytes)).build();
            cookieSet.add(chromeCookie);
        }
    }

    private byte[] decryptBytes(byte[] encryptedValue) {
        try {
            if (WinDPAPI.isPlatformSupported()) {
                WinDPAPI windpapi = WinDPAPI.newInstance(CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
                boolean isV10 = new String(encryptedValue).startsWith(K_ENCRYPTION_VERSION_PREFIX);
                if (!isV10) {
                    return windpapi.unprotectData(encryptedValue);
                } else {
                    if (StringUtils.isEmpty(localStateFileFullPathAndName)) {
                        throw new IllegalArgumentException("Local State is required");
                    }
                    // Retrieve the AES key which is encrypted by DPAPI from Local State
                    String localState = FileUtils.readFileToString(new File(this.localStateFileFullPathAndName));
                    JSONObject jsonObject = new JSONObject(localState);
                    String encryptedKeyBase64 = jsonObject.getJSONObject("os_crypt").getString("encrypted_key");
                    byte[] encryptedKeyBytes = Base64.decodeBase64(encryptedKeyBase64);
                    if (!new String(encryptedKeyBytes).startsWith(K_DPAPI_KEY_PREFIX)) {
                        throw new IllegalStateException("Local State should start with DPAPI");
                    }
                    encryptedKeyBytes = Arrays.copyOfRange(encryptedKeyBytes, K_DPAPI_KEY_PREFIX.length(), encryptedKeyBytes.length);

                    // Use DPAPI to get the real AES key
                    byte[] keyBytes = windpapi.unprotectData(encryptedKeyBytes);
                    if (keyBytes.length != KEY_LENGTH) {
                        throw new IllegalStateException("Local State key length is wrong");
                    }

                    // Obtain the nonce.
                    byte[] nonceBytes = Arrays.copyOfRange(encryptedValue, K_ENCRYPTION_VERSION_PREFIX.length(), K_ENCRYPTION_VERSION_PREFIX.length() + IV_LENGTH);

                    // Strip off the versioning prefix before decrypting.
                    encryptedValue = Arrays.copyOfRange(encryptedValue, K_ENCRYPTION_VERSION_PREFIX.length() + IV_LENGTH, encryptedValue.length);

                    // Use BC provider to decrypt
                    return getDecryptBytes(encryptedValue, keyBytes, nonceBytes);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString(), e.fillInStackTrace());
        }
        return null;
    }

    public byte[] getDecryptBytes(byte[] inputBytes, byte[] keyBytes, byte[] ivBytes) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            return cipher.doFinal(inputBytes);
        } catch (Exception ex) {
            logger.error(ex.toString(), ex.fillInStackTrace());
        }
        return null;
    }
}
