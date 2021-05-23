package webdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;

import java.io.*;
import java.io.FileReader;
import java.util.*;
import java.util.ArrayList;

public class IndexWriter {

    private ArrayList<String> tokenSet;
    private ArrayList<String> productsSet;

    public ArrayList<String> getTokenSet() {
        return this.tokenSet;
    }

    private void second_read (String inputFile, FileSaver saver){
        File file = new File(inputFile);
        int numReview = 0;
        String last_product_id = "";
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String curProductId = "";
            int curHelpfulnessNumerator = 0;
            int curHelpfulnessDenominator = 0;
            int curScore = 0;
            int curLength = 0;
            int curP = 0;
            while ((line = br.readLine()) != null) {
                if (! line.isEmpty()) {

                    if (line.startsWith("product/productId:")){
                        String[] arrOfStr = line.split(": ", 2);
                        curProductId = arrOfStr[1];
                        numReview += 1;
                        curP = this.productsSet.indexOf(curProductId);
                    }
                    else if (line.startsWith("review/helpfulness:")){
                        String[] arrOfStr = line.split(": ", 2);
                        String[] curHelpfulness = arrOfStr[1].split("/", 2);
                        curHelpfulnessNumerator = Integer.parseInt(curHelpfulness[0]);
                        curHelpfulnessDenominator = Integer.parseInt(curHelpfulness[1]);
                    }
                    else if (line.startsWith("review/score:")){
                        String[] arrOfStr = line.split(": ", 2);
                        double d = Double.parseDouble(arrOfStr[1]);
                        curScore = (int) d;
                    }
                    else if (line.startsWith("review/text:")){
                        line = line.replace("review/text: ", "");
                        String review = line.toLowerCase(Locale.ROOT);
                        String[] words = review.split("[^a-z0-9]+", 0);
                        curLength = words.length;
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                int token_n = this.tokenSet.indexOf(word);

                                saver.saveTokenReview(token_n, numReview);
                            }
                        }
                        saver.saveReview(curScore, curHelpfulnessNumerator, curHelpfulnessDenominator, curLength, curP);
                    }
                }
            }
            fr.close();    //closes the stream and release the resources
            saver.endTokenReview(this.tokenSet.size());
            // TODO: call token dictionary saver
            // tokensSet, numBytesperToken, numBytesperReview
            saver.endSaving();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void first_read (String inputFile, FileSaver saver){

        File file=new File(inputFile);
        this.tokenSet = new ArrayList<String>();  // set of all tokens to give IDS
        ArrayList<Product> products = new ArrayList<Product>();
        this.productsSet = new ArrayList<String>();
        int numTokensAll = 0;
        int numReview = 0;
        String last_product_id = "";
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String curProductId = "";
            Product curP = null;
            while ((line = br.readLine()) != null) {
                if (! line.isEmpty()) {
                    if (line.startsWith("product/productId:")){
                        String[] arrOfStr = line.split(": ", 2);
                        curProductId = arrOfStr[1];
                        numReview += 1;
                        if (!last_product_id.equals(curProductId)){
                            last_product_id = curProductId;
                            if (!products.isEmpty()){
                                curP.setLastReviewId(numReview-1);
                            }
                            curP = new Product(curProductId, numReview);
                            products.add(curP);
                            this.productsSet.add(curProductId);
                        }
                    }
                    else if (line.startsWith("review/text:")){
                        line = line.replace("review/text: ", "");
                        String review = line.toLowerCase(Locale.ROOT);
                        String[] words = review.split("[^a-z0-9]+", 0);
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                boolean per = this.tokenSet.contains(word);
                                if (!per){  // if the word didnt exist in the hashtable before
                                    this.tokenSet.add(word);
                                    numTokensAll += 1;
                                }
                            }
                        }
                    }
                }
            }
            fr.close();    //closes the stream and release the resources
            curP.setLastReviewId(numReview);

            Collections.sort(this.tokenSet);
            Collections.sort(products);
            Collections.sort(this.productsSet);

            // calls to FileSaver
            saver.saveGeneralData(numTokensAll, numReview);
            saver.saveAllProducts(products);
            saver.update_byteAmountProducts(numReview);
            saver.writeByteAmountAndMore();

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void write(String inputFile, String dir) {
        // if needed create the directory in which all index files will be created
        File directory = new File(dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        FileSaver saver = new FileSaver(dir);
        first_read(inputFile, saver);
        second_read (inputFile, saver);
    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directoryToBeDeleted = new File(dir);
        this.deleteDirectory(directoryToBeDeleted);
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
