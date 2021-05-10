package webdata;

/**
 * This class keeps the information of the number of bytes that is required for storing different parts of the data.
 */
public class ByteAmount {

    private int bytes_per_frequency, bytes_per_collection_frequency, bytes_per_concatenated_ptr,
            bytes_per_inverted_ptr, bytes_per_product_num, bytes_per_product_id;

    public static int BYTES_PER_PRODUCT_ID = 10;

    public ByteAmount (){
        this.bytes_per_frequency = 0;
        this.bytes_per_collection_frequency = 0;
        this.bytes_per_concatenated_ptr = 0;
        this.bytes_per_inverted_ptr = 0;
        this.bytes_per_product_num = 0;
        this.bytes_per_product_id = BYTES_PER_PRODUCT_ID;
    }

    public ByteAmount (int bytes_per_freq, int bytes_per_collection_freq, int bytes_per_concat_ptr,
                       int bytes_per_inverted_ptr, int bytes_per_product_num, int bytes_per_product_id){
        this.bytes_per_frequency = bytes_per_freq;
        this.bytes_per_collection_frequency = bytes_per_collection_freq;
        this.bytes_per_concatenated_ptr = bytes_per_concat_ptr;
        this.bytes_per_inverted_ptr = bytes_per_inverted_ptr;
        this.bytes_per_product_num = bytes_per_product_num;
        this.bytes_per_product_id = bytes_per_product_id;
    }

    public void setPerFrequency(int byte_num) {
        this.bytes_per_frequency = byte_num;
    }

    public int getPerFrequency() {
        return this.bytes_per_frequency;
    }

    public void setPerCollectionFrequency(int byte_num) {
        this.bytes_per_collection_frequency = byte_num;
    }

    public int getPerCollectionFrequency() {
        return this.bytes_per_collection_frequency;
    }

    public void setPerConcatenatedPtr(int byte_num) {
        this.bytes_per_concatenated_ptr = byte_num;
    }

    public int getPerConcatenatedPtr() {
        return this.bytes_per_concatenated_ptr;
    }

    public void setPerInvertedPtr(int byte_num) {
        this.bytes_per_inverted_ptr = byte_num;
    }

    public int getPerInvertedPtr() {
        return this.bytes_per_inverted_ptr;
    }

    public void setPerProductNum(int byte_num) {
        this.bytes_per_product_num = byte_num;
    }

    public int getPerProductNum() {
        return this.bytes_per_product_num;
    }

    public int getPerProductID() {
        return this.bytes_per_product_id;
    }

}
