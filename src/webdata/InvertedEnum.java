package webdata;

import java.util.Enumeration;
import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * This class is an enumerator that iterates over integers of the form id-1, freq-1, id-2, freq-2, ...
 * such that id-n is the n-th review containing the given token (sorted) and freq-n is the
 * number of times that the token appears in review id-n.
 */
public class InvertedEnum implements Enumeration<Integer> {

    private RandomAccessFile inverted_index;
    private int start_ptr, end_ptr;
    private boolean is_file_index; // every other value is an index of a file (rather than a
    // frequency of appearances in the file).
    private int prev_file;

    public InvertedEnum (String inverted_index_path, int start_ptr, int end_ptr){
        this.is_file_index = true;
        this.prev_file = 0;
        try {
            this.inverted_index = new RandomAccessFile(inverted_index_path, "r");
            this.start_ptr = start_ptr;
            if (end_ptr == -1) {
                this.end_ptr = (int) this.inverted_index.length();
            }
            else {
                this.end_ptr = end_ptr;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasMoreElements() {
        return (this.start_ptr < this.end_ptr);
    }

    private int readInvertedFile(int numBytes, int whereSeek){
        byte[] bytes = new byte[numBytes];
        try{
            this.inverted_index.seek(whereSeek);
            this.inverted_index.read(bytes,0,numBytes);
            return generalFunctions.byteToInt(bytes, numBytes);
        }
        catch (IOException e){
            return -1;
        }
    }

    @Override
    public Integer nextElement() {
        if (hasMoreElements()){
            try {
                // find out number of bytes used for storing the value
                byte[] temp = new byte[1];
                this.inverted_index.seek(this.start_ptr);
                this.inverted_index.read(temp, 0, 1);
                int num_of_bytes = (((temp[0])>>6) & 0x03) + 1;
                // reading the value and reducing the first 2 bits (that were used for storing the number of bytes)
                int num = this.readInvertedFile(num_of_bytes, this.start_ptr);
                if (num == -1) {    return null;}
                if (num_of_bytes == 2 || num_of_bytes == 4) {
                    num -= Math.pow(2, 8 * (num_of_bytes-1) + 6);
                }
                if (num_of_bytes == 3 || num_of_bytes == 4) {
                    num -= Math.pow(2, 8 * (num_of_bytes-1) + 7);
                }
                // updating fields as a preparation for reading the next value
                this.start_ptr += num_of_bytes;
                if (this.is_file_index) {
                    this.is_file_index = false;
                    this.prev_file += num;
                    return this.prev_file;
                }
                this.is_file_index = true;
                return num;
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            this.inverted_index.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}


