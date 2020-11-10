package MedicusCore;

public class EncryptionCore implements EncryptionInterface
{
    public byte[] Encrypt(byte[] password) //maybe these public classes need to be encapsulated
    {

        byte[] E = new byte[password.length];

        for (int x = 0; x < password.length; x++)
        {
            E[x] = (byte) ((x % 2 == 0) ? password[x] + 1 : password[x] - 1);
        }
        return E;
    }

    @Override
    public byte[] Decrypt(byte[] password)
    {
        byte[] D = new byte[password.length];
        for (int j = 0; j < password.length; j++)
        {
            D[j] = (byte) ((j % 2 == 0) ? password[j] - 1 : password[j] + 1);
        }
        return D;
    }
}
