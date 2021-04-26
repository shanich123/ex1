package webdata;

import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IndexReader {

    private RandomAccessFile reviews, inverted_index, product_review, review_product,
            concatenated_list, general_data, product_ids;
    private RandomAccessFile token_dict;
    private ByteAmount byte_amount;
    private int numReviews = -1;
    private int tokenSize = -1;
    private int numOfProducts = -1;
    private int num_bytesProducts = -1;
    private int num_bytesReviews = -1;

    public static String REVIEW = "/review_file";
    public static String INVERTED = "/inverted_index";
    public static String PRODUCT_IDS= "/product_review";
    public static String REVIEW_PRODUCT = "/review_product";
    public static String CONCATENATED = "/concatenated_list";
    public static String GENERAL = "/general_data";
    public static String TOKENS = "/token_dict";

    public static int BYTESIZEREVIEW = 5;

    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        // open files to read from
        try {
            this.reviews = new RandomAccessFile(dir+REVIEW, "r");
            this.inverted_index = new RandomAccessFile(dir+INVERTED, "r");
            this.review_product = new RandomAccessFile(dir+REVIEW_PRODUCT, "r");
            this.concatenated_list = new RandomAccessFile(dir+CONCATENATED, "r");
            this.general_data = new RandomAccessFile(dir+GENERAL, "r");
            this.token_dict = new RandomAccessFile(dir+TOKENS, "r");
            this.product_ids = new RandomAccessFile(dir+PRODUCT_IDS, "r");
            this.byte_amount = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // read general data
        byte[] bytes = new byte[4];
        byte[] bytes1 = new byte[1];
        try{
            this.general_data.seek(0);
            int r = this.general_data.read(bytes,0,4);
            this.tokenSize = ByteBuffer.wrap(bytes).getInt();
            this.general_data.seek(4);
            r = this.general_data.read(bytes,0,4);
            this.numReviews = ByteBuffer.wrap(bytes).getInt();
            this.general_data.seek(8);
            r = this.general_data.read(bytes1,0,1);
            this.num_bytesProducts = generalFunctions.byteToInt(bytes1, 1);
            this.general_data.seek(9);
            r = this.general_data.read(bytes1,0,1);
            this.num_bytesReviews = generalFunctions.byteToInt(bytes1, 1);
            r = this.general_data.read(bytes,0,4);
            this.numOfProducts = ByteBuffer.wrap(bytes).getInt();
            this.general_data.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }


    private int readReviewFile (int reviewId, int numBytes, int whereSeek){
        byte[] bytes = new byte[numBytes];
        if (reviewId>this.numReviews || reviewId<1){
            return -1;
        }
        try{
            this.review_product.seek(whereSeek);
            int r = this.review_product.read(bytes,0,numBytes);
            int reviewScore = generalFunctions.byteToInt(bytes, numBytes);
            return reviewScore;
        }
        catch (IOException e){
            return -1;
        }
    }

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId) {
        int id = readReviewFile(reviewId, 1, (BYTESIZEREVIEW+ this.num_bytesProducts)*(reviewId-1)+5);
        if (id == -1){
            return null;
        }
        try{
            byte[] bytes = new byte[10];
            this.product_ids.seek(id*(10+2*this.num_bytesReviews));
            int r = this.product_ids.read(bytes,0,10);
            String curProduct = new String(bytes, StandardCharsets.UTF_8);
            return curProduct;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+ this.num_bytesProducts)*(reviewId-1));
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+ this.num_bytesProducts)*(reviewId-1)+1);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+ this.num_bytesProducts)*(reviewId-1)+2);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return readReviewFile(reviewId, 2, (BYTESIZEREVIEW+ this.num_bytesProducts)*(reviewId-1)+3);
    }

    private void setByteAmount() {
        byte[] temp = new byte[4];
        int bytes_per_freq, bytes_per_collection_freq, bytes_per_concat_ptr, bytes_per_inverted_ptr;
        try{
            this.general_data.seek(0);
//            bytes_per_freq = ByteBuffer.wrap(this.general_data.read(temp)).getInt();
//            bytes_per_collection_freq = ByteBuffer.wrap(this.general_data.read(temp)).getInt();
//            bytes_per_concat_ptr = ByteBuffer.wrap(this.general_data.read(temp)).getInt();
//            bytes_per_inverted_ptr = ByteBuffer.wrap(this.general_data.read(temp)).getInt();
//          this.byte_amount = ByteAmount(bytes_per_freq, bytes_per_collection_freq, bytes_per_concat_ptr, bytes_per_inverted_ptr);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private int tokenPtr(int tokenNum) {
        int num_of_first_in_block, num_of_last_in_block, num_of_middle_in_block;
        int bytes_for_first, bytes_for_last, bytes_for_middle;
        if (this.byte_amount == null) {
            this.setByteAmount();
        }
        num_of_first_in_block = (int)Math.ceil(tokenNum / 8);
        num_of_last_in_block = (int)Math.floor(tokenNum / 8);
        num_of_middle_in_block = tokenNum - (num_of_first_in_block + num_of_last_in_block);
        bytes_for_first = this.byte_amount.getPerFrequency() + this.byte_amount.getPerCollectionFrequency() + this.byte_amount.getPerConcatenatedPtr() + 1 + this.byte_amount.getPerInvertedPtr();
        bytes_for_last = this.byte_amount.getPerFrequency() + this.byte_amount.getPerCollectionFrequency() + 1 + this.byte_amount.getPerInvertedPtr();
        bytes_for_middle = this.byte_amount.getPerFrequency() + this.byte_amount.getPerCollectionFrequency() + 1 + 1 + this.byte_amount.getPerInvertedPtr();
        return (num_of_first_in_block * bytes_for_first) + (num_of_last_in_block * bytes_for_last) + (num_of_middle_in_block * bytes_for_middle);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        return 0;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        return -1;
    }

    /**
     * Return a series of integers of the form id-1, freq-1, id-2, freq-2, ... such
     * that id-n is the n-th review containing the given token and freq-n is the
     * number of times that the token appears in review id-n
     * Only return ids of reviews that include the token
     * Note that the integers should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews containing this token
     */
     public Enumeration<Integer> getReviewsWithToken(String token) {
         return null;
     }

     /**
     * Return the number of product reviews available in the system
     */
    public int getNumberOfReviews() {
        return this.numReviews;
    }

    /**
     * Return the number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews() {
        return this.tokenSize;
    }

    private int binarySearchProducts(String key){
        int first = 0;
        int last = this.numOfProducts;
        byte[] bytes = new byte[10];
        int mid = (first + last)/2;
        try{
            while( first <= last ){
                this.product_ids.seek(mid*(10+2*this.num_bytesReviews));
                int r = this.product_ids.read(bytes,0,10);
                String curProduct = new String(bytes, StandardCharsets.UTF_8);
                int compare = curProduct.compareTo(key);
                if (compare < 0){
                    first = mid + 1;
                }
                else if ( curProduct.equals(key)){
                    return mid;
                }
                else{
                    last = mid - 1;
                }
                mid = (first + last)/2;
            }
            return -1;
        }
        catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Return the ids of the reviews for a given product identifier
     * Note that the integers returned should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews for this product
     */
    public Enumeration<Integer> getProductReviews(String productId) {
        byte[] bytes = new byte[this.num_bytesReviews];
        int id = binarySearchProducts(productId);
        if (id == -1){
            return null;
        }
        try{
            this.product_ids.seek(id*(10+2*this.num_bytesReviews) + 10);
            int r = this.product_ids.read(bytes,0,this.num_bytesReviews);
            Integer first = generalFunctions.byteToInt(bytes, this.num_bytesReviews);
            this.product_ids.seek(id*(10+2*this.num_bytesReviews) + 10 + this.num_bytesReviews);
            r = this.product_ids.read(bytes,0,this.num_bytesReviews);
            Integer last = generalFunctions.byteToInt(bytes, this.num_bytesReviews);
            webEnum returnV = new webEnum(first, last);
            return returnV;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

}