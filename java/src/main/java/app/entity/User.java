package app.entity;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Created by Ovidiu on 10/24/2016.
 */
public class User {

    private String _id;
    private String ip;
    private String endpoint;
    private String key;
    private String token;
    private long timestamp;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PublicKey getUserPublicKey() {
        try {
            KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            ECPoint point = ecSpec.getCurve().decodePoint(Base64.getDecoder().decode(key));
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);

            return kf.generatePublic(pubSpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getUserPublicKey(String key) {
        try {
            KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            ECPoint point = ecSpec.getCurve().decodePoint(Base64.getDecoder().decode(key));
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);

            return kf.generatePublic(pubSpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
