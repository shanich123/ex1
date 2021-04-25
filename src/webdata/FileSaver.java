package webdata;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.lang.Math;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class FileSaver {

    private BufferedOutputStream reviews, inverted_index, product_review, review_product,
            concatenated_list, general_data, product_ids;
    private BufferedOutputStream token_dict;
    private ByteAmount byte_amount;
    private int num_bytesReviews, num_bytesProducts;

    public static String REVIEW = "/review_file";
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
            FileOutputStream review_file = new FileOutputStream(dir+REVIEW);
            this.reviews = new BufferedOutputStream(review_file);

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
//            BufferedOutputStream dict_z = new BufferedOutputStream(token_dict_z_file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.byte_amount = new ByteAmount();
    }

    public void endSaving (){
        try {
            this.reviews.close();
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
                            int productNum, int num_bytesProducts){
        // review saved while reading the input file
        //  helpfulnessNumerator 1 byte helpfulnessDenominator 1 byte ReviewLength 2 byte score 1 byte
        try{
            byte[] scoreB = generalFunctions.integerToBytes(score, 1);
            this.review_product.write(scoreB);
            byte[] helpfulnessNumeratorB = generalFunctions.integerToBytes(helpfulnessNumerator, 1);
            this.review_product.write(helpfulnessNumeratorB);
            byte[] helpfulnessDenominatorB = generalFunctions.integerToBytes(helpfulnessDenominator, 1);
            this.review_product.write(helpfulnessDenominatorB);
            byte[] ReviewLengthB = generalFunctions.integerToBytes(ReviewLength, 2);
            this.review_product.write(ReviewLengthB);
            byte[] productNumB = generalFunctions.integerToBytes(productNum, num_bytesProducts);
            this.review_product.write(productNumB);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllReviews (ArrayList<Review> allReviews, int productNum){
        this.num_bytesProducts = generalFunctions.calculateNumBytes(productNum);
        for (int i = 0; i <= allReviews.size()-1; i++){
            Review cur = allReviews.get(i);
            saveReview(cur.getScore(), cur.getHelpfulnessNumerator(), cur.getHelpfulnessDenominator(),
                    cur.getReviewLength(), cur.getProductNum(), num_bytesProducts);
        }
    }

    private void saveProduct (String productId, int firstReviewId, int lastReviewId, int num_bytesR){
        try{
            byte[] byteArrray = productId.getBytes();
            this.product_ids.write(byteArrray);
            byte[] review_id = generalFunctions.integerToBytes(firstReviewId, num_bytesR);
            this.product_review.write(review_id);
            byte[] product_id = generalFunctions.integerToBytes(lastReviewId, num_bytesR);
            this.product_review.write(product_id);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllProducts (ArrayList<Product> products, int numMaxReviewId){
        // save all products dict decide how many bytes for firstReviewId according to numMaxReviewId and num products
        this.num_bytesReviews = generalFunctions.calculateNumBytes(numMaxReviewId);
        for (int i = 0; i <= products.size()-1; i++){
            Product cur = products.get(i);
            saveProduct(cur.getProductId(), cur.getFirstReviewId(), cur.getLastReviewId(), num_bytesReviews);
        }
    }

    private int bytesToRepresentNum(int num) {
        return (int) Math.ceil((Math.log(num) / Math.log(2))) / 8;
    }

    public void generalData (int tokenSizeOfReviews, int numberOfReviews){
        // 4 bytes tokenSizeOfReviews + 4 bytes numberOfReviews
        byte [] sizeBytes = ByteBuffer.allocate(4).putInt(tokenSizeOfReviews).array();
        byte [] numBytes = ByteBuffer.allocate(4).putInt(numberOfReviews).array();
        byte [] n1 = generalFunctions.integerToBytes(this.num_bytesProducts, 1);
        byte [] n2 = generalFunctions.integerToBytes(this.num_bytesReviews, 1);
        try{
            this.general_data.write(sizeBytes,0,4);
            this.general_data.write(numBytes,0,4);
            this.general_data.write(n1);
            this.general_data.write(n2);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        //TODO: replace
        this.byte_amount.setPerFrequency(bytesToRepresentNum(numberOfReviews));
        this.byte_amount.setPerCollectionFrequency(bytesToRepresentNum(tokenSizeOfReviews));
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

    private int writeToConcatenatedList(Token token) {
        String str = token.getWord();
        int len = str.length();
        int pref_size = token.getPrefixSize();
        String substr = str.substring(pref_size, len);
        int substr_len = 2*(len-pref_size);
        try {
            byte[] substr_bytes = substr.getBytes("UTF-16"); // 2 bytes for each character
            this.concatenated_list.write(substr_bytes, token.getConcatenatedCursor(), substr_len);
            this.concatenated_list.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return substr_len;
    }

    private int writeInvertedList(Token token) {
        // encode inverted list, write into this.inverted_index (note the inverted_list_cursor) and return number of bytes used.
        try {
            this.inverted_index.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 0; //TODO: ??????????????
    }

    // can only be called after calling generalData which updates this.byte_amount
    public void saveAllTokens (ArrayList<String> tokenSet, Hashtable<String, Token> tokens){
        int concatenated_list_cursor = 0;
        int inverted_list_cursor = 0;
        int dict_cursor = 0;

        // save the for each token the number byte for concatenated_list and inverted index
        // as well the size of the prefix

        for (int i = 1; i <= tokenSet.size(); i++) {
            String token_str = tokenSet.get(i-1);
            Token token = tokens.get(token_str);
            token.setConcatenatedCursor(concatenated_list_cursor);
            token.setInvertedCursor(inverted_list_cursor);
            if (i % 8 == 1) {  // if first in a block
                token.setPrefixSize(0);
            }
            else {
                token.setPrefixSize(sizeOfCommonPrefix(token_str, tokenSet.get(i-2)));
            }
            // saves the concatenated_list and all inverted_index and update the pointer
            concatenated_list_cursor += this.writeToConcatenatedList(token);
            inverted_list_cursor += this.writeInvertedList(token);
        }


        this.byte_amount.setPerConcatenatedPtr(bytesToRepresentNum(concatenated_list_cursor));
        this.byte_amount.setPerInvertedPtr(bytesToRepresentNum(inverted_list_cursor));

        int bytes_per_frequency = this.byte_amount.getPerFrequency();
        int bytes_per_collection_frequency = this.byte_amount.getPerCollectionFrequency();
        int bytes_per_concatenated_ptr = this.byte_amount.getPerConcatenatedPtr();
        int bytes_per_inverted_ptr = this.byte_amount.getPerInvertedPtr();
        try {
            for (int i = 1; i <= tokenSet.size(); i++) {
                String token_str = tokenSet.get(i-1);
                Token token = tokens.get(token_str);
                byte [] frequency = generalFunctions.integerToBytes(token.getFrequency(), bytes_per_frequency);
                this.token_dict.write(frequency);
                dict_cursor += bytes_per_frequency;
                byte [] collection_frequency = generalFunctions.integerToBytes(token.getCollectionFrequency(), bytes_per_collection_frequency);
                this.token_dict.write(collection_frequency);
                dict_cursor += bytes_per_collection_frequency;
                if (i % 8 == 1) {
                    byte []ptr_to_concatenated_list = generalFunctions.integerToBytes(token.getConcatenatedCursor(), bytes_per_concatenated_ptr);
                    this.token_dict.write(ptr_to_concatenated_list);
                    dict_cursor += bytes_per_concatenated_ptr;
                }
                else {
                    byte [] prefix_size = generalFunctions.integerToBytes(token.getPrefixSize(), 1);
                    this.token_dict.write(prefix_size);
                    dict_cursor += 1;
                }
                if (!(i % 8 == 0)) {
                    byte [] len = generalFunctions.integerToBytes(2*(token_str.length()-token.getPrefixSize()), 1); // number of bytes = 2 * (length of string - prefix)
                    this.token_dict.write(len);
                    dict_cursor += 1;
                }
                byte [] ptr_to_inverted_list = generalFunctions.integerToBytes(token.getInvertedCursor(), bytes_per_inverted_ptr);
                this.token_dict.write(ptr_to_inverted_list);
                dict_cursor += bytes_per_inverted_ptr;
            }
            this.token_dict.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
