package org.spin.security;

public enum MessageDigestAlgorithms {

    /**
     * The MD2 message digest algorithm defined in RFC 1319.
     */
    MD2("MD2"),

    /**
     * The MD5 message digest algorithm defined in RFC 1321.
     */
    MD5("MD5"),

    /**
     * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
     */
    SHA_1("SHA-1"),

    /**
     * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
     */
    SHA_256("SHA-256"),

    /**
     * The SHA-384 hash algorithm defined in the FIPS PUB 180-2.
     */
    SHA_384("SHA-384"),

    /**
     * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
     */
    SHA_512("SHA-512");

    private String _value;

    MessageDigestAlgorithms(String _value) {
        this._value = _value;
    }

    public String value() {
        return this._value;
    }
}
