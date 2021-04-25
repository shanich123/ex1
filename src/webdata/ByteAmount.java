package webdata;

public class ByteAmount {

    private int bytes_per_frequency, bytes_per_collection_frequency, bytes_per_concatenated_ptr, bytes_per_inverted_ptr;

    public ByteAmount (){
        this.bytes_per_frequency = 0;
        this.bytes_per_collection_frequency = 0;
        this.bytes_per_concatenated_ptr = 0;
        this.bytes_per_inverted_ptr = 0;
    }

    public ByteAmount (int bytes_per_freq, int bytes_per_collection_freq, int bytes_per_concat_ptr, int bytes_per_inverted_ptr){
        this.bytes_per_frequency = bytes_per_freq;
        this.bytes_per_collection_frequency = bytes_per_collection_freq;
        this.bytes_per_concatenated_ptr = bytes_per_concat_ptr;
        this.bytes_per_inverted_ptr = bytes_per_inverted_ptr;
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

}
