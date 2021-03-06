import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/*
    | Author: http://github.com/itsGhost | @_GGhost
    | Type: Proof of Concept
    | License:
    |  Copyright (C) itsghost.me - All Rights Reserved
    |  Unauthorized copying of this file, via any medium is strictly prohibited
    |  Proprietary, private and confidential
*/

public class Main {
    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

    private String[] args;

    public Main(String[] args) {
        this.args = args;
    }

    String key = "YMM8C_H7KCQ2S_KL";
    String id = "PROD0090YUAUV{2B";
    String nonce = "11462468141886834010";
    
    public void run() throws Exception {
        byte[] nonceHash = getMD5(nonce + key);
        String md5Nonce = DatatypeConverter.printHexBinary(nonceHash);
        long[] md5Array = getIntsFromBytes(nonceHash, 4, true).stream().mapToLong(i -> i).toArray();
        byte[] idHash = padEight(nonce + id).getBytes();
        long[] prodIdArr = getIntsFromBytes(idHash).stream().mapToLong(i -> i).toArray();

        long high = 0;
        long low = 0;
        int i = 0;
        while (i < prodIdArr.length - 1) {
            long temp = (md5Array[0] * (((0x0E79A9C1 * prodIdArr[i]) % 0x7FFFFFFF) + high) + md5Array[1]) % 0x7FFFFFFF;
            high = (md5Array[2] * ((prodIdArr[i + 1] + temp) % 0x7FFFFFFF) + md5Array[3]) % 0x7FFFFFFF;
            low = low + high + temp;
            i += 2;
        }
        high = (high + md5Array[1]) % 0x7FFFFFFF;
        low = (low + md5Array[3]) % 0x7FFFFFFF;

        String highHex = longTo8PaddedHex(high);
        String lowHex = longTo8PaddedHex(low);

        //ugh
        highHex = highHex.substring(6, 8) + highHex.substring(4, 6) + highHex.substring(2, 4) + highHex.substring(0, 2);
        lowHex = lowHex.substring(6, 8) + lowHex.substring(4, 6) + lowHex.substring(2, 4) + lowHex.substring(0, 2);

        high = Long.parseLong(highHex, 16);
        low = Long.parseLong(lowHex, 16);

        //See the old c++ thingy skynet used
        long a = getSection(md5Nonce, 0, 8, high);
        long b = getSection(md5Nonce, 8, 16, low);
        long c = getSection(md5Nonce, 16, 24, high);
        long d = getSection(md5Nonce, 24, 32, low);

        System.out.println(getHexFromSection(a, b, c, d));
    }

    public String getHexFromSection(long... items) {
        StringBuilder sb = new StringBuilder();
        for (long entry : items)
            sb.append(Long.toHexString(entry));
        return sb.toString();
    }

    public long getSection(String md5, int a, int b, long xor) {
        return Long.parseLong(md5.substring(a, b), 16) ^ xor;
    }

    public String longTo8PaddedHex(long l) {
        String str = Long.toHexString(l);
        while (!(str.length() % 8 == 0))
            str = "0" + str;
        return str;
    }

    public String padEight(String str, Boolean after) {
        while (!(str.length() % 8 == 0))
            str = after ? str + "0" : "0" + str;
        return str;
    }

    public String padEight(String str) {
        return this.padEight(str, false);
    }

    public List<Integer> getIntsFromBytes(byte[] bytes, int length, boolean AND) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < length; i++)
            list.add(AND ? bb.getInt() & 0x7FFFFFFF : bb.getInt());
        return list;
    }

    public List<Integer> getIntsFromBytes(byte[] bytes) {
        List<Integer> ints = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        try {
            while (true)
                ints.add(bb.getInt());
        } catch (Exception e) {

        } finally {
            return ints;
        }
    }

    public byte[] getMD5(String str) throws Exception {
        return MessageDigest.getInstance("MD5").digest(str.getBytes());
    }
}
