package webdata;


import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ex1_test {
    private static String in_file_name = "ex1_test.txt";
    private static String index_folder_name = "temp";
    private static String referance_file = "output";
    private static IndexReader user_index_reader = null;
    private static BufferedReader referance = null;
    private static int numOfReviews = 0;
    private static int tokenSizeOfReviews = 0;
    static String[] prodIds = null;
    static String[] scores = null;
    static String[] helpfullness_nums = null;
    static String[] helpfullness_denominator = null;
    static String[] lenghts = null;
    static String[] token_values = null;
    static String[] token_iterator_values = null;
    static String[] product_reviews=null;



    @BeforeClass
    public static void createIndex() throws Exception {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite(in_file_name, index_folder_name);
        user_index_reader = new IndexReader(index_folder_name);
        referance = new BufferedReader(new FileReader(referance_file));
        numOfReviews = Integer.parseInt(data_from_line(referance.readLine()," ")[0].strip());
        tokenSizeOfReviews = Integer.parseInt(data_from_line(referance.readLine()," ")[0].strip());
        prodIds = data_from_line(referance.readLine()," ");
        scores = data_from_line(referance.readLine()," ");
        helpfullness_nums = data_from_line(referance.readLine()," ");
        helpfullness_denominator = data_from_line(referance.readLine()," ");
        lenghts = data_from_line(referance.readLine()," ");
        token_values = data_from_line(referance.readLine(),"/");
        token_iterator_values = data_from_line(referance.readLine(),"/");
        product_reviews=data_from_line(referance.readLine(),"/");
    }

    ///helper functions
    public static String[] data_from_line(String line,String sep) {
        String[] splited=line.split(":")[1].split(sep);
        return splited;
    }

    private static int[] parse_freq_val(String vals) {
        String[] splited = vals.split("-")[1].split(" ");
        int token_freq = Integer.parseInt(splited[0]);
        int token_collection = Integer.parseInt(splited[1]);
        int[] out = {token_freq, token_collection};
        return out;
    }

    //tests

    @Test
    public void numOfReviews_tester() {
        String msg = "Error number of Reviews : got " + user_index_reader.getNumberOfReviews() + " expected " + numOfReviews;
        assertEquals(msg, numOfReviews, user_index_reader.getNumberOfReviews());
    }

    @Test
    public void tokenSizeOfReviews_tester() {
        String msg = "Error token Size Of Reviews : got " + user_index_reader.getTokenSizeOfReviews() + " expected " + tokenSizeOfReviews;
        assertEquals(msg, tokenSizeOfReviews, user_index_reader.getTokenSizeOfReviews());
    }


    @Test
    public void getProduct_Id_tester() {
        for (int i = 0; i < numOfReviews - 1; i++) {
            String msg = "Error at Review " + i + "Pid : got " + user_index_reader.getProductId(i) + " expected " + prodIds[i];
            assertEquals(msg, prodIds[i], user_index_reader.getProductId(i + 1));

        }
    }

    @Test
    public void getReview_score_tester() {
        for (int i = 0; i < numOfReviews - 1; i++) {
            String msg = "Error at Review " + i + " score : got " + String.valueOf(user_index_reader.getReviewScore(i + 1)) + " expected " + scores[i];
            assertEquals(msg, Integer.parseInt(scores[i]), user_index_reader.getReviewScore(i + 1));
        }
    }

    @Test
    public void get_helpfulness_num_tester() {
        for (int i = 0; i < numOfReviews - 1; i++) {
            String msg = "Error at Review " + i + "helpfulness numerator : got " + String.valueOf(user_index_reader.getReviewHelpfulnessNumerator(i + 1)) + " expected " + helpfullness_nums[i];
            assertEquals(msg, Integer.parseInt(helpfullness_nums[i]), user_index_reader.getReviewHelpfulnessNumerator(i + 1));
        }
    }

    @Test
    public void get_helpfulness_denominator_tester() {
        for (int i = 0; i < numOfReviews - 1; i++) {
            String msg = "Error at Review " + i + "helpfulness denomerator : got " + String.valueOf(user_index_reader.getReviewHelpfulnessNumerator(i + 1)) + " expected " + helpfullness_nums[i];
            assertEquals(msg, Integer.parseInt(helpfullness_denominator[i]), user_index_reader.getReviewHelpfulnessDenominator(i + 1));
        }
    }

    @Test
    public void get_Review_length_tester() {
        for (int i = 0; i < numOfReviews - 1; i++) {
            String msg = "Error at Review " + i + " length : got " + String.valueOf(user_index_reader.getReviewLength(i + 1)) + " expected " + lenghts[i];
            assertEquals(msg, Integer.parseInt(lenghts[i]), user_index_reader.getReviewLength(i + 1));
        }
    }

    @Test
    public void reviews_with_token_tester()  {
        String[] word_bank = {"to", "food"};
        for (int i = 0; i < word_bank.length; i++) {
            String s = word_bank[i];
            String[] reviews_vals = token_iterator_values[i].split("-")[1].split(" ");
            Enumeration<Integer> reviews_iterator = user_index_reader.getReviewsWithToken(s);
            int index = 0;
            while (reviews_iterator.hasMoreElements() && index < reviews_vals.length) {
                Integer user_output = reviews_iterator.nextElement();
                Integer ref_val = Integer.parseInt(reviews_vals[index]);
                String msg = "while iterating reviews with token "+s+" got error on returned values ";
                assertEquals(msg, ref_val, user_output);
                index++;
            }
            assertFalse("getReviewsWithToken returned too much reviews, expected - " + String.valueOf(reviews_vals.length - 1), reviews_iterator.hasMoreElements());
            assertFalse("getReviewsWithToken didn't returned enough reviews,  expected - " + String.valueOf(reviews_vals.length - 1), index < reviews_vals.length );
        }
    }


    @Test
    public void tokens_value_method_tester() {
        String[] word_bank = {"to", "food"};
        for (int i = 0; i < word_bank.length; i++) {
            String s = word_bank[i];
            int[] values = parse_freq_val(token_values[i]);
            String msg_1 = "Error at function getTokenFrequency on token " + s + ": got " + String.valueOf(user_index_reader.getTokenFrequency(s)) + " expected " + values[0];
            assertEquals(msg_1, values[0],user_index_reader.getTokenFrequency(s) );
            String msg_2 = "Error at function getTokenCollectionFrequency on token " + s + ": got " + String.valueOf(user_index_reader.getTokenCollectionFrequency(s)) + " expected " + values[1];
            assertEquals(msg_2,values[1], user_index_reader.getTokenCollectionFrequency(s));
        }
    }

    @Test
    public void product_reviews_tester(){
        String [] prodId_bank={"B001E4KFG0","B000LQOCH0"};
        for (int i=0;i<prodId_bank.length;i++){
            String s=prodId_bank[i];
            String[] reviews_vals=product_reviews[i].split("-")[1].split(" ");
            Enumeration<Integer> product_iterator=user_index_reader.getProductReviews(s);
            int index =0;
            while(product_iterator.hasMoreElements() && index<reviews_vals.length ){
                Integer user_output=product_iterator.nextElement();
                Integer ref_val=Integer.parseInt(reviews_vals[index]);
                String msg = "while iterating product ids -"+s+" got error on returned values ";
                assertEquals(msg, ref_val, user_output);
                index++;
            }
            assertFalse("getReviewsWithToken returned too much reviews, expected - " + String.valueOf(reviews_vals.length - 1), product_iterator.hasMoreElements());
            assertFalse("getReviewsWithToken didn't returned enough reviews,  expected - " + String.valueOf(reviews_vals.length - 1), index < reviews_vals.length );
        }
    }
}