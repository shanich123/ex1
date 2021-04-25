package webdata;
import java.io.*;
import java.io.FileReader;
import java.util.*;
import java.util.ArrayList;


public class SlowIndexWriter {

    private String greatestCommonPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(0, i);
            }
        }
        return a.substring(0, minLength);
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        // if needed create the directory in which all index files will be created
        File directory = new File(dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        //read raw data
        FileSaver saver = new FileSaver(dir);
        File file=new File(inputFile);
        Hashtable<String, Token> tokens = new Hashtable<String, Token>();
        ArrayList<String> tokenSet = new ArrayList<String>();
        ArrayList<Product> products = new ArrayList<Product>();
        ArrayList<String> productsSet = new ArrayList<String>();
        ArrayList<Review> reviews = new ArrayList<Review>();
        int numTokensAll = 0;
        int numDiffTokens = 0;
        int numReview = 0;
        String last_product_id = "";
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String curProductId = "";
            int curProductNum = -1;
            int numMaxReviewId = 0;
            int curHelpfulnessNumerator = 0;
            int curHelpfulnessDenominator = 0;
            int curScore = 0;
            int curLength = 0;
            Product curP = null;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()){
//                    saver.saveReview(curScore, curHelpfulnessNumerator, curHelpfulnessDenominator, curLength);
                    Review cur = new Review(curScore, curHelpfulnessNumerator, curHelpfulnessDenominator, curLength,
                            curProductId);
                    reviews.add(cur);
                }
                else {
                    String[] arrOfStr = line.split(": ", 2);
                    if (line.startsWith("product/productId:")){
                        curProductId = arrOfStr[1];
                        numReview += 1;
                        curProductNum += 1;
                        if (!last_product_id.equals(curProductId)){
                            last_product_id = curProductId;
                            numMaxReviewId = numReview;
                            if (!products.isEmpty()){
                                curP.setLastReviewId(numReview-1);
                            }
                            curP = new Product(curProductId, numReview);
                            products.add(curP);
                            productsSet.add(curProductId);
                        }
                    }
                    else if (line.startsWith("review/helpfulness:")){
                        String[] curHelpfulness = arrOfStr[1].split("/", 2);
                        curHelpfulnessNumerator = Integer.parseInt(curHelpfulness[0]);
                        curHelpfulnessDenominator = Integer.parseInt(curHelpfulness[1]);
                    }
                    else if (line.startsWith("review/score:")){
                        double d = Double.parseDouble(arrOfStr[1]);
                        curScore = (int) d;
                    }
                    else if (line.startsWith("review/text:")){
                        String review = arrOfStr[1];
                        String[] words = review.split("[^a-zA-Z0-9']+", 0);
                        curLength = words.length;
                        numTokensAll += curLength;
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                word = word.toLowerCase(Locale.ROOT);
                                boolean per = tokenSet.contains(word);
                                if (!per){  // if the word didnt exist in the hashtable before
                                    tokenSet.add(word);
                                    Token newT = new Token (word, 1, 1, numReview);
                                    tokens.put(word, newT);
                                    numDiffTokens += 1;
                                }
                                else {
                                    Token update = tokens.get(word);
                                    update.setFrequency(numReview);
                                }
                            }
                        }
                    }
                }
            }
            fr.close();    //closes the stream and release the resources


            Collections.sort(tokenSet);
            Collections.sort(products);
            Collections.sort(productsSet);
            // TODO: remove prints
            System.out.println("number of tokens with repeats: " + numTokensAll);
            System.out.println("number of different tokens: " + numDiffTokens);
            System.out.println("number of reviews: " + numReview);
            // calls to FileSaver

            for (int i=0; i<=reviews.size()-1; i++){
                Review cur = reviews.get(i);
                String curPr = cur.getProductID();
                int index = productsSet.indexOf(curPr);
                cur.setProductNum(index);
            }
            saver.saveAllReviews(reviews, products.size()-1);
            saver.saveAllProducts(products, numMaxReviewId);
            saver.generalData(numDiffTokens, numReview);
//            saver.saveAllTokens(tokenSet, tokens);
            saver.endSaving();
        }
        catch (IOException e){
            e.printStackTrace();
        }

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