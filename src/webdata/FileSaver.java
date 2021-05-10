package webdata;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.lang.Math;

public class FileSaver {

    private BufferedOutputStream inverted_index, product_review, review_product,
            concatenated_list, general_data, product_ids, token_dict;
    private ByteAmount byte_amount;
    private int numOfProducts, numOfTokens;

    public static String INVERTED = "/inverted_index";
    public static String PRODUCT_REVIEW = "/product_review";
    public static String PRODUCT_IDS= "/product_review";
    public static String REVIEW_PRODUCT = "/review_product";
    public static String CONCATENATED = "/concatenated_list";
    public static String GENERAL = "/general_data";
    public static String TOKENS = "/token_dict";

    public FileSaver(String dir){
        // open files to save in
        try {

            FileOutputStream inverted_index_file = new FileOutputStream(dir+INVERTED);
            this.inverted_index = new BufferedOutputStream(inverted_index_file);

            FileOutputStream product_review_file = new FileOutputStream(dir+PRODUCT_REVIEW);
            this.product_review = new BufferedOutputStream(product_review_file);

            FileOutputStream product_ids_file = new FileOutputStream(dir+PRODUCT_IDS);
            this.product_ids = new BufferedOutputStream(product_ids_file);

            FileOutputStream review_product_file = new FileOutputStream(dir+REVIEW_PRODUCT);
            this.review_product = new BufferedOutputStream(review_product_file);

            FileOutputStream concatenated_list_file = new FileOutputStream(dir+CONCATENATED);
            this.concatenated_list = new BufferedOutputStream(concatenated_list_file);

            FileOutputStream general_data_file = new FileOutputStream(dir+GENERAL);
            this.general_data = new BufferedOutputStream(general_data_file);

            FileOutputStream token_dict_file = new FileOutputStream(dir+TOKENS);
            this.token_dict = new BufferedOutputStream(token_dict_file);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.byte_amount = new ByteAmount();
    }

    // writes general information to the general_data file, including number of bytes that are required for
    // storing different parts of the data, and the number of tokens and products.
    public void writeByteAmountAndMore (){
        try {
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerFrequency(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerCollectionFrequency(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerConcatenatedPtr(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerInvertedPtr(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerProductNum(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerProductID(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.numOfProducts, 4));
            this.general_data.write(generalFunctions.integerToBytes(this.numOfTokens, 4));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endSaving (){
        try {
            this.inverted_index.close();
            this.product_review.close();
            this.review_product.close();
            this.concatenated_list.close();
            this.general_data.close();
            this.token_dict.close();
            this.product_ids.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveReview (int score, int helpfulnessNumerator, int helpfulnessDenominator, int ReviewLength,
                             int productNum){
        // review saved while reading the input file
        //  score: 1 byte, helpfulnessNumerator: 1 byte, helpfulnessDenominator: 1 byte, ReviewLength: 2 byte,
        //  productNum: number of bytes depends on data.
        try{
            byte[] scoreB = generalFunctions.integerToBytes(score, 1);
            this.review_product.write(scoreB);
            byte[] helpfulnessNumeratorB = generalFunctions.integerToBytes(helpfulnessNumerator, 1);
            this.review_product.write(helpfulnessNumeratorB);
            byte[] helpfulnessDenominatorB = generalFunctions.integerToBytes(helpfulnessDenominator, 1);
            this.review_product.write(helpfulnessDenominatorB);
            byte[] ReviewLengthB = generalFunctions.integerToBytes(ReviewLength, 2);
            this.review_product.write(ReviewLengthB);
            byte[] productNumB = generalFunctions.integerToBytes(productNum, this.byte_amount.getPerProductNum());
            this.review_product.write(productNumB);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllReviews (ArrayList<Review> allReviews){
        this.byte_amount.setPerProductNum(generalFunctions.calculateNumBytes(this.numOfProducts));
        for (int i = 0; i <= allReviews.size()-1; i++){
            Review cur = allReviews.get(i);
            saveReview(cur.getScore(), cur.getHelpfulnessNumerator(), cur.getHelpfulnessDenominator(),
                    cur.getReviewLength(), cur.getProductNum());
        }
    }

    private void saveProduct (String productId, int firstReviewId, int lastReviewId){
        try{
            byte[] byteArray = productId.getBytes();
            this.product_ids.write(byteArray);
            byte[] first_id = generalFunctions.integerToBytes(firstReviewId, this.byte_amount.getPerFrequency());
            this.product_ids.write(first_id);
            byte[] last_id = generalFunctions.integerToBytes(lastReviewId, this.byte_amount.getPerFrequency());
            this.product_ids.write(last_id);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllProducts (ArrayList<Product> products){
        // save all products dict decide how many bytes for firstReviewId according to numMaxReviewId and num products
        this.numOfProducts =  products.size()-1;
        for (int i = 0; i <= products.size()-1; i++){
            Product cur = products.get(i);
            saveProduct(cur.getProductId(), cur.getFirstReviewId(), cur.getLastReviewId());
        }
    }

    public void saveGeneralData (int tokenSizeOfReviews, int numberOfReviews){
        // tokenSizeOfReviews: 4 bytes, numberOfReviews: 4 bytes
        byte [] sizeBytes = ByteBuffer.allocate(4).putInt(tokenSizeOfReviews).array();
        byte [] numBytes = ByteBuffer.allocate(4).putInt(numberOfReviews).array();
        try{
            this.general_data.write(sizeBytes,0,4);
            this.general_data.write(numBytes,0,4);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        this.byte_amount.setPerFrequency(generalFunctions.calculateNumBytes(numberOfReviews));
        this.byte_amount.setPerCollectionFrequency(generalFunctions.calculateNumBytes(tokenSizeOfReviews));
    }

    private int sizeOfCommonPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return i;
            }
        }
        return minLength;
    }

    // writes token to the concatenated-list file, using the front-coding technique (save each token without prefix).
    // Returns the number of bytes used in the concatenated-list file.
    private int writeToConcatenatedList(Token token) {
        String str = token.getWord();
        int len = str.length();
        int pref_size = token.getPrefixSize();
        String substr = str.substring(pref_size, len);
        int substr_len = len-pref_size;
        try {
            byte[] substr_bytes = substr.getBytes();
            this.concatenated_list.write(substr_bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return substr_len;
    }

    // Writes the given number to the inverted-index file, in the length-precoded varint method.
    private int writeNumToInvertedList(int num) {
        String bits = Integer.toBinaryString(num);
        int len = bits.length() + 2;
        int num_of_bytes = (int) Math.ceil((double)len/8);
        String num_str = "";
        if (num_of_bytes == 1) { num_str = "00";}
        if (num_of_bytes == 2) { num_str = "01";}
        if (num_of_bytes == 3) { num_str = "10";}
        if (num_of_bytes == 4) { num_str = "11";}
        String str = num_str + "0".repeat( 8*num_of_bytes - len) + bits;
        int val = Integer.parseInt(str, 2);
        byte[] to_write = generalFunctions.integerToBytes(val, num_of_bytes);
        try {
            this.inverted_index.write(to_write);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return num_of_bytes;
    }

    // encodes the gaps between the files in which the token appears, and the frequencies of the token's
    // appearances in each of these files.
    // Returns the number of bytes used in the inverted-index file.
    private int writeInvertedList(Token token) {
        ArrayList<Integer> invertedIndex = token.getInvertedIndex();
        ArrayList<Integer> invertedFrequency = token.getInvertedFrequency();
        int prev_index = 0;
        int gap, freq;
        int bytes_used = 0;
        for (int i = 0; i < invertedIndex.size(); i++) {
            gap = invertedIndex.get(i) - prev_index;
            freq = invertedFrequency.get(i);
            bytes_used += this.writeNumToInvertedList(gap);
            bytes_used += this.writeNumToInvertedList(freq);
            prev_index += gap;
        }
        return bytes_used;
    }

    public void saveAllTokens (ArrayList<String> tokenSet, Hashtable<String, Token> tokens){
        this.numOfTokens = tokens.size();

        // writes to the concatenated-list and the inverted-index, and for each token - saves pointers
        // to those files, and saves size of common prefix with the previous token.
        int concatenated_list_cursor = 0;
        int inverted_list_cursor = 0;
        for (int i = 1; i <= tokenSet.size(); i++) {
            String token_str = tokenSet.get(i-1);
            Token token = tokens.get(token_str);
            token.setConcatenatedPtr(concatenated_list_cursor);
            token.setInvertedPtr(inverted_list_cursor);
            if (i % 8 == 1) {  // if first in a block
                token.setPrefixSize(0);
            }
            else {
                token.setPrefixSize(sizeOfCommonPrefix(token_str, tokenSet.get(i-2)));
            }
            // saves the token in the concatenated_list, updates the inverted_index, and increments the
            // pointers in the concatenated-list and inverted-index files.
            concatenated_list_cursor += this.writeToConcatenatedList(token);
            inverted_list_cursor += this.writeInvertedList(token);
        }

        // declares number of bytes required for storing pointers to the concatenated-list and inverted-index files.
        this.byte_amount.setPerConcatenatedPtr(generalFunctions.calculateNumBytes(concatenated_list_cursor));
        this.byte_amount.setPerInvertedPtr(generalFunctions.calculateNumBytes(inverted_list_cursor));

        // number of bytes required for storing different parts of the data
        int bytes_per_frequency = this.byte_amount.getPerFrequency();
        int bytes_per_collection_frequency = this.byte_amount.getPerCollectionFrequency();
        int bytes_per_concatenated_ptr = this.byte_amount.getPerConcatenatedPtr();
        int bytes_per_inverted_ptr = this.byte_amount.getPerInvertedPtr();
        // for each token, writes its entries in the token dictionary. According to the 7-in-8 front coding technique
        // that is used, the following entries are saved for each token: frequency, collection-frequency,
        // pointer to concatenated-list (only for tokens that are first in block), size of common prefix with
        // previous token (not for first in block), length of token without the common prefix (not for last in block),
        // and pointer to inverted-index file.
        try {
            for (int i = 1; i <= tokenSet.size(); i++) {
                String token_str = tokenSet.get(i-1);
                Token token = tokens.get(token_str);
                byte [] frequency = generalFunctions.integerToBytes(token.getFrequency(), bytes_per_frequency);
                this.token_dict.write(frequency);
                byte [] collection_frequency = generalFunctions.integerToBytes(token.getCollectionFrequency(),
                        bytes_per_collection_frequency);
                this.token_dict.write(collection_frequency);
                if (i % 8 == 1) {
                    byte[] ptr_to_concatenated_list = generalFunctions.integerToBytes(token.getConcatenatedPtr(),
                            bytes_per_concatenated_ptr);
                    this.token_dict.write(ptr_to_concatenated_list);
                }
                else {
                    byte [] prefix_size = generalFunctions.integerToBytes(token.getPrefixSize(), 1);
                    this.token_dict.write(prefix_size);
                }
                if (!(i % 8 == 0)) {
                    byte [] len = generalFunctions.integerToBytes(token_str.length()-token.getPrefixSize(), 1);
                    this.token_dict.write(len);
                }
                byte [] ptr_to_inverted_list = generalFunctions.integerToBytes(token.getInvertedPtr(),
                        bytes_per_inverted_ptr);
                this.token_dict.write(ptr_to_inverted_list);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
