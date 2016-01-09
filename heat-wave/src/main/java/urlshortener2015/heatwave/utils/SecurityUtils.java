package urlshortener2015.heatwave.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

	/**
	 * Gets the hashed string of a text, using the SHA-512 algorithm
	 * @param text Text to hash
	 * @return Hashed string
	 * 
	 * @author <a href="https://github.com/teruyi/UrlShortener2015/tree/master/bangladesh-green">Bangladesh-green</a>
	 * @see <a href="https://github.com/teruyi/UrlShortener2015/blob/master/bangladesh-green/src/main/java/urlshortener/bangladeshgreen/secure/Hash.java#L14">Original method</a>
	 */
    public static String hashSHA512(String text) {
        MessageDigest md;
        String response = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes("UTF-8"));
            byte[] digest = md.digest();
            // Formats result string to hexadecimal with left zero padding (for better management).
            response = String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Fatal error: could not encrypt text with SHA-512 algorithm.");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Fatal error: UnsupportedEncodingException making hash from text.");
        }
        return response;
    }
}
