package webdata;
import java.io.*;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.FileOutputStream;
import java.util.Random;

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

    public static void main(String[] args) {

        SlowIndexWriter a = new SlowIndexWriter();
        a.slowWrite("1000.txt", "C:\\IndexDirectory");
//        checkReviews100 ();
//        checkProduct100 ();
    }
}
