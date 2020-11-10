package MedicusCore;

public interface EncryptionInterface
{
    byte[] Encrypt(byte[] password);

    byte[] Decrypt(byte[] password);
}
