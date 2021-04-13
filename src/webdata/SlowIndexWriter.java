package webdata;
import java.io.*;
import java.io.FileReader;
import java.util.*;

public class SlowIndexWriter {

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
        File file=new File(inputFile);    //read raw data
        Hashtable<String, Integer> tokens = new Hashtable<String, Integer>();
        Hashtable<Integer, String> my_dict = new Hashtable<Integer, String>();
        int numReviews = 0;
        int numTokensAll = 0;
        int numDiffTokens = 0;
        int averageLen = 0;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String curProductId = "";
            String curHelpfulness = "";
            String curScore = "";
            Integer curLength = 0;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()){
                    numReviews += 1;
                    String finalS = curProductId + "&" + curHelpfulness + "&" + curScore + "&" + curLength.toString();
                    my_dict.put(numDiffTokens, finalS);
                }
                else {
                    if (line.startsWith("product/productId:")){
                        String[] arrOfStr = line.split(":", 2);
                        curProductId = arrOfStr[1];
                    }
                    else if (line.startsWith("review/helpfulness:")){
                        String[] arrOfStr = line.split(":", 2);
                        curHelpfulness = arrOfStr[1];
                    }
                    else if (line.startsWith("review/score:")){
                        String[] arrOfStr = line.split(":", 2);
                        curScore = arrOfStr[1];
                    }
                    else if (line.startsWith("review/text:")){
                        String[] arrOfStr = line.split(":", 2);
                        String review = arrOfStr[1];
                        String[] words = review.split("[^a-zA-Z0-9']+", 0);
                        curLength = words.length;
                        numTokensAll += curLength;
                        for (String word : words) {
                            Integer per = tokens.get(word);
                            if (per != null){
                                tokens.put(word, per+1);
                            }
                            else {  // if the word didnt exist in the hashtable before
                                numDiffTokens += 1;
                                tokens.put(word, 1);
                                averageLen += word.length();
                            }
                        }
                    }
                }
            }
            fr.close();    //closes the stream and release the resources
            System.out.println("number of reviews: " + numReviews);
            System.out.println("number of tokens with repeats: " + numTokensAll);
            System.out.println("number of different tokens: " + numDiffTokens);
            System.out.println("average token length: " + (averageLen/numDiffTokens));
            int averageF = 0;
            Set<String> keys = tokens.keySet();
            for(String key: keys){
                averageF += tokens.get(key);
//                System.out.println("Value of "+key+" is: "+tokens.get(key));
            }
            System.out.println("average token frequency: " + (averageF/numDiffTokens));
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