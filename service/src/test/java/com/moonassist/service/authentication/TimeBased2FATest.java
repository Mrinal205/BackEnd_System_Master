package com.moonassist.service.authentication;

import com.google.zxing.WriterException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class TimeBased2FATest {

  private String secretKey = "quu6 ea2g horg md22 sn2y ku6v kisc kyag";
  private String email = "eric@moonassist.com";
  private String company = "MoonAssist";

  private TimeBased2FA timeBased2FA = new TimeBased2FA();

  @Test
  public void testGetRandomSecretKey() {
    String newSecrectKey = timeBased2FA.getRandomSecretKey();
    Assert.assertNotNull(newSecrectKey);
  }

  @Test
  public void testGetGoogleAuthenticatorBarCode() {

    String bar = timeBased2FA.getGoogleAuthenticatorBarCode(secretKey, "user@moonassist.com", "MoonAssist");
    Assert.assertNotNull(bar);
    Assert.assertEquals("otpauth://totp/MoonAssist%3Auser%40moonassist.com?secret=QUU6EA2GHORGMD22SN2YKU6VKISCKYAG&issuer=MoonAssist", bar);
  }

  @Ignore
  @Test
  public void testCreateQRCode() throws IOException, WriterException {

    String barCodeString = timeBased2FA.getGoogleAuthenticatorBarCode(secretKey, email, company);
    timeBased2FA.createQRCode(barCodeString, "/Users/ericanderson/Desktop/2FA.png", 200, 200);
  }

  /** nice utility for creating QR codes manually
   *  OR https://dan.hersam.com/tools/gen-qr-code.html
   **/
  @Ignore
  @Test
  public void testCreateQRCode_manual() throws IOException, WriterException {

    String secret = "6KMB2BWZFKH6GWJTUF4UUVP3XZWRF6CA";
    secret = secret.replace(" ", "").toUpperCase();

    String barCodeString = "otpauth://totp/MoonAssist%3Arogier%40abc.d?secret=" + secret + "&issuer=MoonAssist";
    timeBased2FA.createQRCode(barCodeString, "/Users/ericanderson/Desktop/2FA.png", 200, 200);
  }

  @Test
  public void testGenerateCode() {

    String code = timeBased2FA.getTOTPCode(secretKey);
    Assert.assertNotNull(code);
  }

}
