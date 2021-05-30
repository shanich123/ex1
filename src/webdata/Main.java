package webdata;
import java.util.*;

public class Main {


    public static  void checkBytes100 (){
        int i = 112;
        byte[] in4 = generalFunctions.integerToBytes(i,4);
        System.out.println(in4.length);
        byte[] in3 = generalFunctions.integerToBytes(i,3);
        System.out.println(in3.length);
        byte[] in2 = generalFunctions.integerToBytes(i,2);
        System.out.println(in2.length);
        byte[] in1 = generalFunctions.integerToBytes(i,1);
        System.out.println(in1.length);

        System.out.println(generalFunctions.byteToInt(in4, 4));
        System.out.println(generalFunctions.byteToInt(in3, 3));
        System.out.println(generalFunctions.byteToInt(in2, 2));
        System.out.println(generalFunctions.byteToInt(in1, 1));
    }

    public static void checkReviews100 (){
        IndexReader b = new IndexReader("try");

        System.out.println(b.getProductId(0) == null);
        System.out.println(b.getReviewScore(0) == -1);
        System.out.println(b.getReviewScore(1001) == -1);

        System.out.println(b.getProductId(2).compareTo("B00813GRG4") == 0);
        System.out.println(b.getReviewScore(2) == 1);
        System.out.println(b.getReviewHelpfulnessDenominator(2) == 0);
        System.out.println(b.getReviewHelpfulnessNumerator(2) == 0);
        System.out.println(b.getReviewLength(2) == 32);

        System.out.println(b.getProductId(1000).compareTo("B006F2NYI2") == 0);
        System.out.println(b.getReviewScore(1000) == 2);
        System.out.println(b.getReviewHelpfulnessDenominator(1000) == 5);
        System.out.println(b.getReviewHelpfulnessNumerator(1000) == 2);
        System.out.println(b.getReviewLength(1000) == 102);
    }

    public static void checkProduct100 (){
        IndexReader b = new IndexReader("try");
        Enumeration<Integer> t = b.getProductReviews("B006F2NYI2");
        System.out.println(b.getProductReviews("B006F2NYI2").toString().compareTo("webEnum{first=988, last=1000}") == 0);
        System.out.println(b.getProductReviews("B006K2ZZ7K").toString().compareTo("webEnum{first=5, last=8}") == 0);
    }

    private static void printReviewData(IndexReader b, int reviewId){
        System.out.println("data for review: " + reviewId);
        System.out.println("score: " + b.getReviewScore(reviewId));
        System.out.println("helpfulness numerator: " + b.getReviewHelpfulnessNumerator(reviewId));
        System.out.println("helpfulness denominator: " + b.getReviewHelpfulnessDenominator(reviewId));
        System.out.println("length: " + b.getReviewLength(reviewId));
        System.out.println("product id: " + b.getProductId(reviewId));
    }

    private static void printProductData(IndexReader b, String productId) {
        System.out.println("reviews for product: " + productId);
        Enumeration<Integer> reviewEnum = b.getProductReviews(productId);
        for (Enumeration<Integer> t = reviewEnum; t.hasMoreElements(); ) {
            System.out.println(t.nextElement());
        }
    }

    private static void printTokenData(IndexReader b, String token){
        System.out.println("data for token: " + token);
        System.out.println("frequency: " + b.getTokenFrequency(token));
        System.out.println("collection frequency: " + b.getTokenCollectionFrequency(token));
        System.out.println("reviews,frequencies for token: " + token);
        Enumeration<Integer> tokenEnum = b.getReviewsWithToken(token);
        for (Enumeration<Integer> t = tokenEnum; t.hasMoreElements(); ) {
            System.out.println(t.nextElement());
        }
    }

    public static void runtimes (IndexReader r, IndexWriter a){
        long start = System.currentTimeMillis();
        ArrayList<String> tokens = a.getTokenSet();
        for (int i = 1; i <= 100 ; i++) {
            int index = (int)(Math.random() * tokens.size());
            String ask = tokens.get(index);
            r.getReviewsWithToken(ask);
        }
        long end = System.currentTimeMillis();
        long elapsedTime1 = end - start;
        System.out.println(elapsedTime1);
        start = System.currentTimeMillis();
        for (int i = 1; i <= 100 ; i++) {
            int index = (int)(Math.random() * tokens.size());
            String ask = tokens.get(index);
            r.getTokenFrequency(ask);
        }
        end = System.currentTimeMillis();
        long elapsedTime2 = end - start;
        System.out.println(elapsedTime2);
    }


    public static void main(String[] args) {

        IndexWriter a = new IndexWriter();
        //a.write("100.txt", "test");
        //a.write("ex1_test.txt", "test");
        long start = System.currentTimeMillis();
        a.write("/cs/+/course/webdata/1M.txt", "test1");
        long end = System.currentTimeMillis();
        long elapsedTime2 = end - start;
        System.out.println(elapsedTime2);
//        IndexReader b = new IndexReader("test");
//
//        for (int i = 1; i < 4 ; i++) {
//            System.out.println("num " +i);
//            System.out.println("score " + b.getReviewScore(i));
//            System.out.println("product " + b.getProductId(i));
//            System.out.println("num " + b.getReviewHelpfulnessNumerator(i));
//            System.out.println("denum " + b.getReviewHelpfulnessDenominator(i));
//            System.out.println("len " + b.getReviewLength(i));
////            System.out.println("h_num " + b.getReviewHelpfulnessNumerator(i));
//            System.out.println("**********************");
//        }

//        // general data
//        System.out.println("number of tokens with repetitions: " + b.getTokenSizeOfReviews());
//        System.out.println("number of reviews: " + b.getNumberOfReviews());
//        // review data
//        printReviewData(b, 3);
//        // product data
//        printProductData(b, "B00813GRG4");
//        printProductData(b, "B006F2NYI2");
//        // token data
//        printTokenData(b, "0"); // token 1
//        printTokenData(b, "00"); // token 2
//        printTokenData(b, "1");
//        printTokenData(b, "10");
//        printTokenData(b, "100");
//        printTokenData(b, "10lbs");
//        printTokenData(b, "1300watt");
//        printTokenData(b, "16");
//        printTokenData(b, "1845");
//
//        printTokenData(b, "000"); // token 3
//        printTokenData(b, "000kwh"); // token 4
//        printTokenData(b, "042608460503"); // token 5
//        printTokenData(b, "0472066978"); // token 6
//        printTokenData(b, "0738551856"); // token 7
//        printTokenData(b, "09"); // token 8
//        printTokenData(b, "0g"); // token 9
//        printTokenData(b, "a");
//        printTokenData(b, "the");
//        printTokenData(b, "zucchini"); // appears only in the 1000 file, last among tokens
//        printTokenData(b, "zola"); // appears only in the 1000 file, second-last among tokens
//        printTokenData(b, "affection"); // appears only in the 1000 file, first among tokens
//        printTokenData(b, "africafe"); // appears only in the 1000 file, second among tokens
//        printTokenData(b, "suckered"); // appears once in file 999
//        printTokenData(b, "peanuts");
//        printTokenData(b, "confectionery");
//        printTokenData(b, "around");
//
//        a.removeIndex("test");

//        checkReviews100 ();
//        checkProduct100 ();
    }
}
