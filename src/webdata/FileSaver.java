package webdata;

import java.util.ArrayList;
import java.util.Hashtable;

public class FileSaver {

   // BufferedOutputStream reviewDictFile;

    public FileSaver(String dir){
        // open files to save in
        // review dict
        // product dict
    }

    public void saveReview (int score, int helpfulnessNumerator, int helpfulnessDenominator, int ReviewLength){
        // review saved while reading the input file
        //  helpfulnessNumerator 1 byte helpfulnessDenominator 1 byte ReviewLength 1 byte + 5 bit score 3 bit
    }

    private void saveProduct (String productId, int firstReviewId){
        // productId 10 bytes
    }

    public void saveAllProducts (ArrayList<Product> products, int numMaxReviewId){
        // save all products dict decide how many bytes for firstReviewId according to numMaxReviewId
    }

    public void generalData (int tokenSizeOfReviews, int numberOfReviews){
        // 4 bytes tokenSizeOfReviews + 4 bytes numberOfReviews
    }

    public void saveAllToken (ArrayList<String> tokenSet, Hashtable<String, Token> tokens){
        // sorted array list of tokens
    }

}
