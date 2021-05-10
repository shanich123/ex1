package webdata;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Enumeration;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class IndexReader {

    private String dir;
    private RandomAccessFile review_product, concatenated_list, general_data, product_ids, token_dict;
    private ByteAmount byte_amount;
    private int numReviews = -1;
    private int tokenSize = -1;
    private int numOfProducts = -1;
    private int numOfTokens = -1;
    private int bytes_for_first_token, bytes_for_middle_token, bytes_for_last_token;

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
        this.dir = dir;
        // open files to read from
        try {
            this.review_product = new RandomAccessFile(dir+REVIEW_PRODUCT, "r");
            this.concatenated_list = new RandomAccessFile(dir+CONCATENATED, "r");
            this.general_data = new RandomAccessFile(dir+GENERAL, "r");
            this.token_dict = new RandomAccessFile(dir+TOKENS, "r");
            this.product_ids = new RandomAccessFile(dir+PRODUCT_IDS, "r");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // read general data
        byte[] bytes = new byte[4];
        int bytes_per_freq = 0;
        int bytes_per_collection_freq = 0;
        int bytes_per_concat_ptr = 0;
        int bytes_per_inverted_ptr = 0;
        int bytes_per_product_num = 0;
        int bytes_per_product_id = 0;
        try{
            this.general_data.read(bytes,0,4);
            this.tokenSize = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            this.numReviews = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_freq = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_collection_freq = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_concat_ptr = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_inverted_ptr = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_product_num = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            bytes_per_product_id = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            this.numOfProducts = generalFunctions.byteToInt(bytes, 4);
            this.general_data.read(bytes,0,4);
            this.numOfTokens = generalFunctions.byteToInt(bytes, 4);
            this.general_data.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        this.byte_amount = new ByteAmount(bytes_per_freq, bytes_per_collection_freq, bytes_per_concat_ptr,
                bytes_per_inverted_ptr, bytes_per_product_num, bytes_per_product_id);
        this.bytes_for_first_token = this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency() + this.byte_amount.getPerConcatenatedPtr() + 1 +
                this.byte_amount.getPerInvertedPtr();
        this.bytes_for_last_token = this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency() + 1 + this.byte_amount.getPerInvertedPtr();
        this.bytes_for_middle_token = this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency() + 1 + 1 + this.byte_amount.getPerInvertedPtr();
    }


    private int readReviewFile (int reviewId, int numBytes, int whereSeek){
        byte[] bytes = new byte[numBytes];
        if (reviewId>this.numReviews || reviewId<1){
            return -1;
        }
        try{
            this.review_product.seek(whereSeek);
            this.review_product.read(bytes,0,numBytes);
            int reviewRes = generalFunctions.byteToInt(bytes, numBytes);
            return reviewRes;
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
        int id = readReviewFile(reviewId, 1, (BYTESIZEREVIEW+
                this.byte_amount.getPerProductNum())*(reviewId-1)+5);
        if (id == -1){
            return null;
        }
        try{
            byte[] bytes = new byte[this.byte_amount.getPerProductID()];
            this.product_ids.seek(id*(this.byte_amount.getPerProductID()+ 2*this.byte_amount.getPerFrequency()));
            this.product_ids.read(bytes,0,this.byte_amount.getPerProductID());
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
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+
                this.byte_amount.getPerProductNum())*(reviewId-1));
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+
                this.byte_amount.getPerProductNum())*(reviewId-1)+1);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return readReviewFile(reviewId, 1, (BYTESIZEREVIEW+
                this.byte_amount.getPerProductNum())*(reviewId-1)+2);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return readReviewFile(reviewId, 2, (BYTESIZEREVIEW+
                this.byte_amount.getPerProductNum())*(reviewId-1)+3);
    }

    // Returns pointer to the beginning of the row in the token dictionary, that relates to the tokenNum.
    private int tokenPtr(int tokenNum) {
        // the following variables represent the number of tokens before tokenNum,
        // that were first/last/middle in their blocks
        int num_of_first_in_block, num_of_last_in_block, num_of_middle_in_block;
        num_of_first_in_block = (int)Math.ceil((double)tokenNum / 8);
        num_of_last_in_block = (int)Math.floor((double)tokenNum / 8);
        num_of_middle_in_block = tokenNum - (num_of_first_in_block + num_of_last_in_block);
        return (num_of_first_in_block * this.bytes_for_first_token) + (num_of_last_in_block * this.bytes_for_last_token)
                + (num_of_middle_in_block * this.bytes_for_middle_token);
    }

    // For the given block_id, returns pointer to the beginning in the concatenated-list of the first token in
    // the block, and to the beginning of the next token.
    private int[] pointersToConcatenatedList(int block_id) {
        int token_ptr = this.tokenPtr(block_id * 8);
        int bytes_of_token_until_concatenated_ptr = this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency();
        byte[] ptr_bytes = new byte[this.byte_amount.getPerConcatenatedPtr()];
        byte[] len_bytes = new byte[1];
        int start_ptr, len;
        try {
            this.token_dict.seek(token_ptr + bytes_of_token_until_concatenated_ptr);
            this.token_dict.read(ptr_bytes, 0, this.byte_amount.getPerConcatenatedPtr());
            start_ptr = generalFunctions.byteToInt(ptr_bytes, this.byte_amount.getPerConcatenatedPtr());
            this.token_dict.read(len_bytes, 0, 1);
            len = generalFunctions.byteToInt(len_bytes, 1);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return new int[] {start_ptr, start_ptr + len};
    }

    // Use binary-search on the first tokens in each block, to find the block in which the token is stored.
    // First returned value is the block_id in which the token should be.
    // Second returned value is 1 if the token is the first in the block, and 0 otherwise.
    private int[] binarySearchTokenBlocks(String token){
        int first = 0;        int last = (int) Math.floor((double)(this.numOfTokens - 1) / 8);
        int[] pointerLimits;
        int len;
        int mid = (first + last)/2;
        try{
            while( first <= last ){
                // get pointers to the concatenated-list, to the beginning of the token and the
                // beginning of the next token.
                pointerLimits = this.pointersToConcatenatedList(mid);
                if (pointerLimits == null) {
                    return null;
                }
                len = pointerLimits[1] - pointerLimits[0];
                byte[] bytes = new byte[len];
                this.concatenated_list.seek(pointerLimits[0]);
                this.concatenated_list.read(bytes,0, len);
                String curToken = new String(bytes, StandardCharsets.UTF_8);
                int compare = curToken.compareTo(token);
                if (compare < 0){
                    first = mid + 1;
                }
                else if ( compare == 0){
                    return new int[]{mid, 1};
                }
                else{
                    last = mid - 1;
                }
                mid = (first + last)/2;
            }
            return new int[]{last, 0};
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private int readTokenFile(int numBytes, int whereSeek){
        byte[] bytes = new byte[numBytes];
        try{
            this.token_dict.seek(whereSeek);
            this.token_dict.read(bytes,0,numBytes);
            return generalFunctions.byteToInt(bytes, numBytes);
        }
        catch (IOException e){
            return -1;
        }
    }

    private String readFromConcatenatedList(int start_ptr, int end_ptr) {
        int len = end_ptr - start_ptr;
        byte[] bytes = new byte[len];
        try {
            this.concatenated_list.seek(start_ptr);
            this.concatenated_list.read(bytes, 0, len);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Returns the string of the token, given the pointers to the string in the concatenated-list,
    // and the previous string, together with the length of the common prefix.
    private String tokenString(String prev, int pref, int start_ptr, int end_ptr) {
        String str = readFromConcatenatedList(start_ptr, end_ptr);
        if (str == null) {  return null;}
        String pref_str = prev.substring(0, pref);
        return pref_str + str;
    }

    // Receives a token and a block in which this token is supposed to be found, and returns
    // its tokenNum if indeed it is found, and -1 otherwise.
    // Assumes that the token isn't the first in the block.
    private int searchInBlock(String token, int block_id) {
        int[] pointerLimits = this.pointersToConcatenatedList(block_id);
        if (pointerLimits == null) {    return -1;}
        String prevToken = readFromConcatenatedList(pointerLimits[0], pointerLimits[1]); //first token in block
        if (prevToken == null) {    return -1;}
        int start_ptr = pointerLimits[1];
        int pref_ptr = tokenPtr(8 * block_id + 1) + this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency();
        String curToken;
        int pref , len, end_ptr, new_block;
        // goes over the tokens in the block (excluding the first), and checks if they are equal to the given token.
        for (int i = 1; i < 8; i++) {
            pref = this.readTokenFile(1, pref_ptr);
            if (pref == -1) {   return  -1;}
            if (i < 7) {
                len = this.readTokenFile(1, pref_ptr + 1);
                if (len == -1) {    return -1;}
                end_ptr = start_ptr + len;
            }
            else { // last token in block
                new_block = block_id + 1;
                if (this.numOfTokens < 8 * new_block + 1) { // the new block doesn't exist
                    try {   end_ptr = (int) this.concatenated_list.length();}
                    catch (IOException e){
                        e.printStackTrace();
                        return -1;
                    }
                }
                else { // the new block exists
                    pointerLimits = this.pointersToConcatenatedList(new_block);
                    if (pointerLimits == null) {    return -1;}
                    end_ptr = pointerLimits[0];
                }
            }
            curToken = tokenString(prevToken, pref, start_ptr, end_ptr);
            if (curToken == null) {  return -1;}
            if (curToken.equals(token)) {
                return 8 * block_id + i;
            }
            pref_ptr += this.bytes_for_middle_token;
            prevToken = curToken;
            start_ptr = end_ptr;
        }
        return -1;
    }

    // Searches the given token. If found, returns its token-number (id). Otherwise returns -1.
    private int searchToken(String token){
        token = token.toLowerCase(Locale.ROOT);
        int[] res = binarySearchTokenBlocks(token);
        if (res == null) {
            return -1;
        }
        int block_id = res[0];
        if (res[1] == 1) { // the exact string was found
            return 8 * block_id;
        }
        if (block_id == -1) {
            return -1;
        }
        return searchInBlock(token, block_id);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        int token_id = this.searchToken(token);
        if (token_id == -1) {
            return 0;
        }
        int token_ptr = this.tokenPtr(token_id);
        int freq = this.readTokenFile(this.byte_amount.getPerFrequency(), token_ptr);
        if (freq == -1) {
            return 0;
        }
        return freq;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        int token_id = this.searchToken(token);
        if (token_id == -1) {
            return 0;
        }
        int token_ptr = this.tokenPtr(token_id);
        int freq = this.readTokenFile(this.byte_amount.getPerCollectionFrequency(),
                (token_ptr + this.byte_amount.getPerFrequency()));
        if (freq == -1) {
            return 0;
        }
        return freq;
    }

    // Returns the pointer to the inverted-list of the given token, in the inverted-index file.
    private int ptrToInvertedList(int tokenId) {
        int token_ptr = this.tokenPtr(tokenId);
        boolean is_first = (tokenId % 8 == 0);
        boolean is_last = (tokenId % 8 == 7);
        int bytes_of_token_until_inverted_ptr = this.byte_amount.getPerFrequency() +
                this.byte_amount.getPerCollectionFrequency();
        if (is_first) {
            bytes_of_token_until_inverted_ptr += this.byte_amount.getPerConcatenatedPtr();
        }
        else {
            bytes_of_token_until_inverted_ptr += 1;
        }
        if (!is_last) {
            bytes_of_token_until_inverted_ptr += 1;
        }
        byte[] ptr_bytes = new byte[this.byte_amount.getPerInvertedPtr()];
        int ptr;
        try {
            this.token_dict.seek(token_ptr + bytes_of_token_until_inverted_ptr);
            this.token_dict.read(ptr_bytes, 0, this.byte_amount.getPerInvertedPtr());
            ptr = generalFunctions.byteToInt(ptr_bytes, this.byte_amount.getPerInvertedPtr());
        }
        catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        return ptr;
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
        int token_id = this.searchToken(token);
        if (token_id == -1) {
            return new InvertedEnum(this.dir+INVERTED, 0, 0); //empty enumerator
        }
        int ptr_to_inverted_list = this.ptrToInvertedList(token_id);
        if (ptr_to_inverted_list == -1) {  return new InvertedEnum(this.dir+INVERTED, 0, 0);} //empty enumerator
        if (token_id == this.numOfTokens - 1) {
            return new InvertedEnum(this.dir+INVERTED, ptr_to_inverted_list, -1);
        }
        int ptr_to_next_inverted_list = this.ptrToInvertedList(token_id + 1);
        return new InvertedEnum(this.dir+INVERTED, ptr_to_inverted_list, ptr_to_next_inverted_list);
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
        byte[] bytes = new byte[this.byte_amount.getPerProductID()];
        int mid = (first + last)/2;
        try{
            while( first <= last ){
                this.product_ids.seek(mid*(this.byte_amount.getPerProductID()+2*this.byte_amount.getPerFrequency()));
                this.product_ids.read(bytes,0,this.byte_amount.getPerProductID());
                String curProduct = new String(bytes, StandardCharsets.UTF_8);
                int compare = curProduct.compareTo(key);
                if (compare < 0){
                    first = mid + 1;
                }
                else if ( compare == 0){
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
        byte[] bytes = new byte[this.byte_amount.getPerFrequency()];
        int id = binarySearchProducts(productId);
        if (id == -1){
            return new reviewsEnum(1, 0);
        }
        try{
            this.product_ids.seek(id*(this.byte_amount.getPerProductID()+2*this.byte_amount.getPerFrequency()) +
                    this.byte_amount.getPerProductID());
            this.product_ids.read(bytes,0,this.byte_amount.getPerFrequency());
            Integer first = generalFunctions.byteToInt(bytes, this.byte_amount.getPerFrequency());
            this.product_ids.seek(id*(this.byte_amount.getPerProductID()+2*this.byte_amount.getPerFrequency()) +
                    this.byte_amount.getPerProductID() + this.byte_amount.getPerFrequency());
            this.product_ids.read(bytes,0,this.byte_amount.getPerFrequency());
            Integer last = generalFunctions.byteToInt(bytes, this.byte_amount.getPerFrequency());
            reviewsEnum returnV = new reviewsEnum(first, last);
            return returnV;
        }
        catch (IOException e){
            e.printStackTrace();
            return new reviewsEnum(1, 0);
        }
    }

}
