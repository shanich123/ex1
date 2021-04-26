package webdata;

public class generalFunctions {

    public static int calculateNumBytes (int num){
        int count = 0;
        while (num > 0) {
            num = num >> 8;
            count++;
        }
        System.out.println("num_bytes");
        System.out.println(count);
        return count;
    }

    public static int byteToInt(byte[] bytes, int length) {
        int val = 0;
        if(length>4) throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

    public static byte[] integerToBytes(int number, int num_of_bytes){
        if (num_of_bytes == 4){
            return new byte[] {
                    (byte)((number >> 24) & 0xff),
                    (byte)((number >> 16) & 0xff),
                    (byte)((number >> 8) & 0xff),
                    (byte)((number >> 0) & 0xff),
            };
        }
        else if (num_of_bytes == 3){
            return new byte[] {
                    (byte)((number >> 16) & 0xff),
                    (byte)((number >> 8) & 0xff),
                    (byte)((number >> 0) & 0xff),
            };
        }
        else if (num_of_bytes == 2){
            return new byte[] {
                    (byte)((number >> 8) & 0xff),
                    (byte)((number >> 0) & 0xff),
            };
        }
        else{
            return new byte[] {
                    (byte)((number >> 0) & 0xff),
            };
        }
    }
}