package org.ripple.bouncycastle.crypto.util;

import java.io.IOException;

import org.ripple.bouncycastle.asn1.ASN1Encodable;
import org.ripple.bouncycastle.asn1.ASN1Integer;
import org.ripple.bouncycastle.asn1.DERNull;
import org.ripple.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.ripple.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.ripple.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.ripple.bouncycastle.asn1.sec.ECPrivateKey;
import org.ripple.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.ripple.bouncycastle.asn1.x509.DSAParameter;
import org.ripple.bouncycastle.asn1.x9.X962Parameters;
import org.ripple.bouncycastle.asn1.x9.X9ECParameters;
import org.ripple.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.ripple.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.ripple.bouncycastle.crypto.params.DSAParameters;
import org.ripple.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.ripple.bouncycastle.crypto.params.ECDomainParameters;
import org.ripple.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.ripple.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.ripple.bouncycastle.crypto.params.RSAKeyParameters;
import org.ripple.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

/**
 * Factory to create ASN.1 private key info objects from lightweight private keys.
 */
public class PrivateKeyInfoFactory
{
    /**
     * Create a PrivateKeyInfo representation of a private key.
     *
     * @param privateKey the SubjectPublicKeyInfo encoding
     * @return the appropriate key parameter
     * @throws java.io.IOException on an error encoding the key
     */
    public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter privateKey) throws IOException
    {
        if (privateKey instanceof RSAKeyParameters)
        {
            RSAPrivateCrtKeyParameters priv = (RSAPrivateCrtKeyParameters)privateKey;

            return new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPrivateKey(priv.getModulus(), priv.getPublicExponent(), priv.getExponent(), priv.getP(), priv.getQ(), priv.getDP(), priv.getDQ(), priv.getQInv()));
        }
        else if (privateKey instanceof DSAPrivateKeyParameters)
        {
            DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters)privateKey;
            DSAParameters params = priv.getParameters();

            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(params.getP(), params.getQ(), params.getG())), new ASN1Integer(priv.getX()));
        }
        else if (privateKey instanceof ECPrivateKeyParameters)
        {
            ECPrivateKeyParameters priv = (ECPrivateKeyParameters)privateKey;
            ECDomainParameters domainParams = priv.getParameters();
            ASN1Encodable params;
            int orderBitLength;

            if (domainParams == null)
            {
                params = new X962Parameters(DERNull.INSTANCE);      // Implicitly CA
                orderBitLength = priv.getD().bitLength();   // TODO: this is as good as currently available, must be a better way...
            }
            else if (domainParams instanceof ECNamedDomainParameters)
            {
                params = new X962Parameters(((ECNamedDomainParameters)domainParams).getName());
                orderBitLength = domainParams.getCurve().getOrder().bitLength();
            }
            else
            {
                X9ECParameters ecP = new X9ECParameters(
                    domainParams.getCurve(),
                    domainParams.getG(),
                    domainParams.getN(),
                    domainParams.getH(),
                    domainParams.getSeed());

                params = new X962Parameters(ecP);
                orderBitLength = domainParams.getCurve().getOrder().bitLength();
            }

            return new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), new ECPrivateKey(orderBitLength, priv.getD(), params));
        }
        else
        {
            throw new IOException("key parameters not recognised.");
        }
    }
}
