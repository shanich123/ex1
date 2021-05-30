package webdata;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.lang.Math;

public class FileSaver {

    private BufferedOutputStream inverted_index, review_product,
            concatenated_list, general_data, product_ids, token_dict, token_review_out, token_review_ordered_out;
    private BufferedInputStream token_review_ordered_in;
    private RandomAccessFile token_review_in;
    private ByteAmount byte_amount;
    private int numOfProducts, numOfTokens;
    private long numSequencesInDisk, numBlocksInMemory, numBlocksInSequence, numPairsInFirstRoundSequence,
            curPairsInMemory, numBlocksInLastSequence;
    private int numPairsInBlock, numPairsInLastBlock, spareBytesInBlock, sizeOfRawPair, numBlocksInDisk, numPairsInDisk;
    private ArrayList<TokenReview> pairs;
    private boolean ordered_file, need_merge;

    private String dir;

    public static String INVERTED = "/inverted_index";
    public static String PRODUCT_IDS= "/product_review";
    public static String REVIEW_PRODUCT = "/review_product";
    public static String CONCATENATED = "/concatenated_list";
    public static String GENERAL = "/general_data";
    public static String TOKENS = "/token_dict";
    public static String TOKEN_REVIEW = "/token_review";
    public static String TOKEN_REVIEW_ORDERED_1 = "/token_review_ordered_1";
    public static String TOKEN_REVIEW_ORDERED_2 = "/token_review_ordered_2";

    public static int BLOCK = 512;

    private byte[] token_review_block, concatenated_list_block, inverted_list_block;
    private int offset_in_token_review_block, offset_in_concatenated_list_block, offset_in_inverted_list_block;

    public FileSaver(String dir){
        this.dir = dir;
        this.byte_amount = new ByteAmount();
        ordered_file = true;
        need_merge = true;
        try {
            this.general_data = new BufferedOutputStream(new FileOutputStream(dir + GENERAL));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        token_review_block = new byte[BLOCK];
        concatenated_list_block = new byte[BLOCK];
        inverted_list_block = new byte[BLOCK];
        offset_in_token_review_block = BLOCK;
        offset_in_concatenated_list_block = BLOCK;
        offset_in_inverted_list_block = BLOCK;
    }

    // writes general information to the general_data file, including number of bytes that are required for
    // storing different parts of the data, and the number of tokens and products.
    public void writeByteAmountAndMore () {
        try {
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerFrequency(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerCollectionFrequency(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerConcatenatedPtr(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerInvertedPtr(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerProductNum(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.byte_amount.getPerProductID(), 4));
            this.general_data.write(generalFunctions.integerToBytes(this.numOfProducts, 4));
            this.general_data.write(generalFunctions.integerToBytes(this.numOfTokens, 4));
            this.general_data.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endSaving (){
        try {
            this.inverted_index.close();
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


    public void openReviewProductFile(){
        try {
            this.review_product = new BufferedOutputStream(new FileOutputStream(dir+REVIEW_PRODUCT));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeReviewProductFile(){
        try {
            this.review_product.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void openTokenReviewFile(){
        try {
            this.token_review_out = new BufferedOutputStream(new FileOutputStream(dir+TOKEN_REVIEW));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        this.sizeOfRawPair = this.byte_amount.getPerTokenNum() + this.byte_amount.getPerFrequency();
        this.numPairsInBlock = Math.floorDiv(BLOCK, sizeOfRawPair);
        this.spareBytesInBlock = BLOCK % sizeOfRawPair;
        this.numBlocksInMemory = Math.floorDiv(Runtime.getRuntime().freeMemory() - 10000, BLOCK);
        int bytesPerPair = 24; // 16 bytes for header, 2*4 bytes for the 2 integer fields.
        if (System.getProperty("sun.arch.data.model").equals("32")) { // only 8 bytes for header.
            bytesPerPair -= 8;
        }
        /**
         int bytesPerPair = (int) instrumentation.getObjectSize(new TokenReview(2000000000, 2000000000));
         this.numPairsInBlock = Math.floorDiv(BLOCK, bytesPerPair);
         this.numBytesInBlockInDisk = numPairsInBlock * sizeOfRawPair;
         this.numPairsInMemory = numBlocksInMemory * numPairsInBlock;
         this.numBlocksInSequence = numBlocksInMemory;
         */
        int max_pair_objects_in_memory = (int) Math.floorDiv(Runtime.getRuntime().freeMemory() - 10000, bytesPerPair);
        this.numPairsInFirstRoundSequence = max_pair_objects_in_memory - (max_pair_objects_in_memory % numPairsInBlock);
        this.numBlocksInSequence = numPairsInFirstRoundSequence / numPairsInBlock;
        this.numSequencesInDisk = 0;
        this.numBlocksInDisk = 0;
        this.curPairsInMemory = 0;
        pairs = new ArrayList<>();
    }

    public void closeTokenReviewFile(){
        if (curPairsInMemory != 0) {
            savePairsCollection();
        }
        numBlocksInLastSequence = numBlocksInDisk % numBlocksInSequence;
        if (numBlocksInLastSequence == 0) {
            numBlocksInLastSequence = numBlocksInSequence;
        }
        numPairsInLastBlock = numPairsInDisk % numPairsInBlock;
        if (numPairsInLastBlock == 0) {
            numPairsInLastBlock = numPairsInBlock;
        }
        try {
            this.token_review_out.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void writePairsToFile(ArrayList<TokenReview> pairs, BufferedOutputStream file) {
        byte[] toWrite = new byte[BLOCK];
        int offset = 0;
        for (int i=0; i<pairs.size(); i++) {
            TokenReview pair = pairs.get(i);
            generalFunctions.integerToBytesInPlace(pair.getToken(), toWrite, offset, this.byte_amount.getPerTokenNum());
            generalFunctions.integerToBytesInPlace(pair.getReview(), toWrite, offset + this.byte_amount.getPerTokenNum(), this.byte_amount.getPerFrequency());
            offset += sizeOfRawPair;
            if ((i+1) % numPairsInBlock == 0) {
                try {
                    file.write(toWrite);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                offset = 0;
            }
        }
        if (offset > 0) {
            try {
                file.write(toWrite, 0, offset);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }



        /**
        int numWholeBlocks = Math.floorDiv(pairs.size(), numPairsInBlock);
        int sparePairs = pairs.size() % numPairsInBlock;
        byte[] toWrite = new byte[numWholeBlocks * BLOCK + sparePairs * sizeOfRawPair];
        for (int j=0; j<numWholeBlocks; j++) {
            for (int i=0; i<numPairsInBlock; i++) {
                TokenReview pair = pairs.get(j*numPairsInBlock + i);
                generalFunctions.integerToBytesInPlace(pair.getToken(), toWrite, j*BLOCK + i*sizeOfRawPair, this.byte_amount.getPerTokenNum());
                generalFunctions.integerToBytesInPlace(pair.getReview(), toWrite, j*BLOCK + i*sizeOfRawPair + this.byte_amount.getPerTokenNum(), this.byte_amount.getPerFrequency());
            }
        }
        for (int k=0; k<sparePairs; k++) {
            TokenReview pair = pairs.get(numWholeBlocks*numPairsInBlock + k);
            generalFunctions.integerToBytesInPlace(pair.getToken(), toWrite, numWholeBlocks*BLOCK + k*sizeOfRawPair, this.byte_amount.getPerTokenNum());
            generalFunctions.integerToBytesInPlace(pair.getReview(), toWrite, numWholeBlocks*BLOCK + k*sizeOfRawPair + this.byte_amount.getPerTokenNum(), this.byte_amount.getPerFrequency());
        }
        try {
            file.write(toWrite);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
         */
    }

    public void savePairsCollection() {
        Collections.sort(pairs);
        writePairsToFile(pairs, token_review_out);
        numSequencesInDisk += 1;
        numBlocksInDisk += (int) Math.ceil(pairs.size() / (double)numPairsInBlock);
        numPairsInDisk += pairs.size();
    }

    public void saveTokenReview (int token, int review){
        if (curPairsInMemory >= numPairsInFirstRoundSequence) {
            savePairsCollection();
            pairs = new ArrayList<>();
            curPairsInMemory = 0;
        }
        pairs.add(new TokenReview(token, review));
        curPairsInMemory += 1;
    }

    public int readBlockNew(byte[] blocks, int block_num, int sequence, int block_to_read) {
        long startingPoint = ((long) sequence * numBlocksInSequence + block_to_read) * BLOCK;
        try {
            token_review_in.seek(startingPoint);
            int numBytes = token_review_in.read(blocks, block_num * BLOCK, BLOCK);
            return numBytes;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Assumes a.length = b.length.
    // Returns: 0 if ‘a is equal to ‘b, a positive value if ‘a’ is greater than ‘b’,
    // a negative value if ‘b’ is greater than ‘a’
    public int compByteArrays(byte[] a, byte[] b) {
        int cmp;
        int len = a.length;
        for (int i=0; i<len; i++) {
            if ((cmp = Byte.compare(a[i], b[i])) != 0) {
                return cmp;
            }
        }
        return 0;
    }

    // Assumes a.length = b.length.
    // Returns: 0 if ‘a is equal to ‘b, a positive value if ‘a’ is greater than ‘b’,
    // a negative value if ‘b’ is greater than ‘a’
    public int compByteArrays(byte[] a, byte[] b, int a_start, int a_end) {
        int cmp;
        for (int i=0; i<(a_end-a_start); i++) {
            if ((cmp = Byte.compare(a[a_start+i], b[i])) != 0) {
                return cmp;
            }
        }
        return 0;
    }


    public int getMinPair(byte[] blocks, int[] pointers, HashSet<Integer> finished) {
        int min_index = -1;
        boolean first_val = true;
        byte[] tokenBytesMin = new byte[byte_amount.getPerTokenNum()];
        byte[] reviewBytesMin = new byte[byte_amount.getPerFrequency()];
        byte[] tokenBytes, reviewBytes;
        int tokenOffset, reviewOffset;
        int comp;
        for (int i = 0; i < pointers.length; i++) {
            if (finished.contains(i)) {
                continue;
            }
            tokenOffset = i * BLOCK + pointers[i] * sizeOfRawPair;
            reviewOffset = tokenOffset + byte_amount.getPerTokenNum();
            tokenBytes = Arrays.copyOfRange(blocks, tokenOffset, tokenOffset + byte_amount.getPerTokenNum());
            if (first_val) {
                tokenBytesMin = tokenBytes;
                reviewBytesMin = Arrays.copyOfRange(blocks, reviewOffset, reviewOffset + byte_amount.getPerFrequency());
                min_index = i;
                first_val = false;
            }
            else {
                comp = compByteArrays(tokenBytes, tokenBytesMin);
                if (comp <= 0) {
                    reviewBytes = Arrays.copyOfRange(blocks, reviewOffset, reviewOffset + byte_amount.getPerFrequency());
                    if (comp == 0) {
                        if (compByteArrays(reviewBytes, reviewBytesMin) >= 0) {
                            continue;
                        }
                    }
                    tokenBytesMin = tokenBytes;
                    reviewBytesMin = reviewBytes;
                    min_index = i;
                }
            }
        }
        if (first_val) {
            return -1;
        }
        return min_index;
    }

    public void orderBunchOfSequences(byte[] blocks, int firstSeq) {
        byte[] ordered_block = new byte[BLOCK];
        int num_of_ordered_pairs = 0;
        int num_of_sequences_in_merge = blocks.length / BLOCK;
        int[] blockInSeq = new int[num_of_sequences_in_merge];
        int[] pointers = new int[num_of_sequences_in_merge];
        HashSet<Integer> finishedSequences = new HashSet<>();
        while (true) {
            // get minimal pair
            int min_index = getMinPair(blocks, pointers, finishedSequences);
            if (min_index == -1) {
                try {
                    token_review_ordered_out.write(ordered_block, 0, num_of_ordered_pairs * sizeOfRawPair);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            // copy minimal pair into the ordered block
            int offset_in_ordered_block = num_of_ordered_pairs * sizeOfRawPair;
            int offset_in_blocks = min_index * BLOCK + pointers[min_index] * sizeOfRawPair;
            for (int i=0; i<sizeOfRawPair; i++) {
                ordered_block[offset_in_ordered_block + i] = blocks[offset_in_blocks + i];
            }
            // write ordered block to disk (if necessary)
            num_of_ordered_pairs += 1;
            if (num_of_ordered_pairs == numPairsInBlock) {
                try {
                    token_review_ordered_out.write(ordered_block);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                num_of_ordered_pairs = 0;
            }
            // check if the last sequence has just ended.
            if (firstSeq + min_index == numSequencesInDisk - 1) { // last sequence
                if (blockInSeq[min_index] == numBlocksInLastSequence - 1) { // last block
                    if (pointers[min_index] == numPairsInLastBlock - 1) { // block has ended
                        finishedSequences.add(min_index);
                        continue;
                    }
                }
            }
            // switch to next block in the sequence (if necessary)
            pointers[min_index] += 1;
            if (pointers[min_index] == numPairsInBlock) {
                blockInSeq[min_index] += 1;
                if (blockInSeq[min_index] == numBlocksInSequence) {
                    finishedSequences.add(min_index);
                }
                else {
                    pointers[min_index] = 0;
                    int numBytes = readBlockNew(blocks, min_index, firstSeq + min_index, blockInSeq[min_index]);
                    if (! (numBytes > 0)) {
                        finishedSequences.add(min_index);
                    }
                }
            }
        }
    }

    public int mergeRound() {
        int max_sequences_in_merge = (int) numBlocksInMemory - 1;
        int num_of_merges = (int) Math.ceil(numSequencesInDisk / (double) max_sequences_in_merge);
        for (int i=0; i<num_of_merges; i++) {
            int firstSeq = i*max_sequences_in_merge;
            int num_of_sequences_in_merge = max_sequences_in_merge;
            if (firstSeq + max_sequences_in_merge > numSequencesInDisk) {
                num_of_sequences_in_merge = num_of_sequences_in_merge - firstSeq;
            }
            //long num_of_blocks = num_of_sequences_in_merge * numBlocksInSequence;
            //if (i == num_of_merges - 1) {
            //    num_of_blocks = (num_of_sequences_in_merge - 1) * numBlocksInSequence + numBlocksInLastSequence;
            //}
            byte[] blocks = new byte[num_of_sequences_in_merge * BLOCK];
            for (int j=0; j < (num_of_sequences_in_merge - 1); j++) {
                readBlockNew(blocks, j, firstSeq + j, 0);
            }
            orderBunchOfSequences(blocks, firstSeq);
        }
        return num_of_merges;
    }

    public String orderedFileString(boolean isFirst) {
        if (isFirst) {
            return dir + TOKEN_REVIEW_ORDERED_1;
        }
        return dir + TOKEN_REVIEW_ORDERED_2;
    }

    public void orderTokenReview() {
        try {
            this.token_review_in = new RandomAccessFile(dir + TOKEN_REVIEW, "r");
            this.token_review_ordered_out = new BufferedOutputStream(new FileOutputStream(orderedFileString(ordered_file)));
            if (numSequencesInDisk == 1) {
                need_merge = false;
                System.out.println("no merging needed");
                System.out.println(numSequencesInDisk);
            }
            while (numSequencesInDisk > 1) {
                System.out.println("merge round");
                System.out.println(numSequencesInDisk);
                numSequencesInDisk = mergeRound();
                numBlocksInSequence *= (numBlocksInMemory - 1);
                numBlocksInLastSequence = numBlocksInDisk % numBlocksInSequence;
                if (numBlocksInLastSequence == 0) {
                    numBlocksInLastSequence = numBlocksInSequence;
                }
                token_review_in.close();
                token_review_ordered_out.close();
                if (numSequencesInDisk > 1) {
                    ordered_file = !ordered_file;
                    this.token_review_in = new RandomAccessFile(orderedFileString(!ordered_file), "r");
                    this.token_review_ordered_out = new BufferedOutputStream(new FileOutputStream(orderedFileString(ordered_file)));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateByteAmountParameters (int numOfTokens, int tokenTotalLength){
        this.byte_amount.setPerConcatenatedPtr(generalFunctions.calculateNumBytes(tokenTotalLength));
        this.byte_amount.setPerInvertedPtr(4);
        this.numOfTokens = numOfTokens;
        this.byte_amount.setPerTokenNum(generalFunctions.calculateNumBytes(numOfTokens));
    }

    public void saveReview (int score, int helpfulnessNumerator, int helpfulnessDenominator, int ReviewLength,
                             int productNum){
        // review saved while reading the input file
        //  score: 1 byte, helpfulnessNumerator: 1 byte, helpfulnessDenominator: 1 byte, ReviewLength: 2 byte,
        //  productNum: number of bytes depends on data.
        byte[] toWrite = new byte[5 + this.byte_amount.getPerProductNum()];
        generalFunctions.integerToBytesInPlace(score, toWrite, 0, 1);
        generalFunctions.integerToBytesInPlace(helpfulnessNumerator, toWrite, 1, 1);
        generalFunctions.integerToBytesInPlace(helpfulnessDenominator, toWrite, 2, 1);
        generalFunctions.integerToBytesInPlace(ReviewLength, toWrite, 3, 2);
        generalFunctions.integerToBytesInPlace(productNum, toWrite, 5, byte_amount.getPerProductNum());
        try{
            review_product.write(toWrite);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllReviews (ArrayList<Review> allReviews){
        try {
            this.review_product = new BufferedOutputStream(new FileOutputStream(dir+REVIEW_PRODUCT));
            for (int i = 0; i <= allReviews.size() - 1; i++) {
                Review cur = allReviews.get(i);
                saveReview(cur.getScore(), cur.getHelpfulnessNumerator(), cur.getHelpfulnessDenominator(),
                        cur.getReviewLength(), cur.getProductNum());
            }
            this.review_product.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void saveProduct (String productId, int firstReviewId, int lastReviewId){
        byte[] toWrite = new byte[byte_amount.getPerProductID() + 2 * byte_amount.getPerFrequency()];
        byte[] byteArray = productId.getBytes();
        for (int i=0; i<byte_amount.getPerProductID(); i++) {
            toWrite[i] = byteArray[i];
        }
        generalFunctions.integerToBytesInPlace(firstReviewId, toWrite, byte_amount.getPerProductID(), byte_amount.getPerFrequency());
        generalFunctions.integerToBytesInPlace(lastReviewId, toWrite, byte_amount.getPerProductID() + byte_amount.getPerFrequency(), byte_amount.getPerFrequency());
        try{
            product_ids.write(toWrite);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveAllProducts (ArrayList<Product> products){
        // save all products dict decide how many bytes for firstReviewId according to numMaxReviewId and num products
        this.numOfProducts =  products.size()-1;
        this.byte_amount.setPerProductNum(generalFunctions.calculateNumBytes(this.numOfProducts));
        try {
            FileOutputStream product_ids_file = new FileOutputStream(this.dir+PRODUCT_IDS);
            this.product_ids = new BufferedOutputStream(product_ids_file);
            for (int i = 0; i <= products.size() - 1; i++) {
                Product cur = products.get(i);
                saveProduct(cur.getProductId(), cur.getFirstReviewId(), cur.getLastReviewId());
            }
            product_ids.close();
        }
        catch (IOException e){
            e.printStackTrace();
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
            concatenated_list.write(substr_bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return substr_len;
    }

    // Writes the given number to the inverted-index file, in the length-pre-coded variant method.
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
            inverted_index.write(to_write);
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


        // writes to the concatenated-list and the inverted-index, and for each token - saves pointers
        // to those files, and saves size of common prefix with the previous token.
        int concatenated_list_cursor = 0;
        int inverted_list_cursor = 0;
        try {
            FileOutputStream inverted_index_file = new FileOutputStream(this.dir+INVERTED);
            this.inverted_index = new BufferedOutputStream(inverted_index_file);
            FileOutputStream concatenated_list_file = new FileOutputStream(this.dir+CONCATENATED);
            this.concatenated_list = new BufferedOutputStream(concatenated_list_file);
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
            inverted_index.close();
            concatenated_list.close();
        }
        catch (IOException e) {
            e.printStackTrace();
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
            FileOutputStream token_dict_file = new FileOutputStream(dir+TOKENS);
            this.token_dict = new BufferedOutputStream(token_dict_file);
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
            token_dict.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LinkedHashMap<Integer, Integer> readNextTokenReviews(int given_token){
        LinkedHashMap<Integer, Integer> frequencies = new LinkedHashMap<>();
        int file_index;
        byte[] givenTokenB = generalFunctions.integerToBytes(given_token, byte_amount.getPerTokenNum());
        while(true) {
            if (BLOCK - offset_in_token_review_block < sizeOfRawPair) {
                try {
                    token_review_ordered_in.read(token_review_block, 0, BLOCK);
                    offset_in_token_review_block = 0;
                } catch (IOException e) {
                    return null;
                }
            }
            if (compByteArrays(token_review_block, givenTokenB, offset_in_token_review_block, offset_in_token_review_block + byte_amount.getPerTokenNum()) != 0) {
                break;
            }
            file_index = generalFunctions.byteToInt(token_review_block, offset_in_token_review_block + byte_amount.getPerTokenNum(), byte_amount.getPerFrequency());
            frequencies.put(file_index, frequencies.containsKey(file_index) ? frequencies.get(file_index)+1 : 1);
            offset_in_token_review_block += sizeOfRawPair;
        }
        return frequencies;
    }

    // writes token to the concatenated-list file, using the front-coding technique (save each token without prefix).
    // Returns the number of bytes used in the concatenated-list file.
    private int writeToConcatenatedListNew(String str, int pref_size) {
        int len = str.length();
        String substr = str.substring(pref_size, len);
        int substr_len = len-pref_size;
        try {
            byte[] substr_bytes = substr.getBytes();
            concatenated_list.write(substr_bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return substr_len;
    }

    // encodes the gaps between the files in which the token appears, and the frequencies of the token's
    // appearances in each of these files.
    // Returns the number of bytes used in the inverted-index file.
    private int writeInvertedListNew(LinkedHashMap<Integer, Integer> frequencies) {
        int bytes_used = 0;
        int prev_index = 0;
        int gap, freq;
        Set<Integer> keys = frequencies.keySet();
        for (Integer index: keys) {
            gap = index - prev_index;
            freq = frequencies.get(index);
            bytes_used += this.writeNumToInvertedList(gap);
            bytes_used += this.writeNumToInvertedList(freq);
            prev_index += gap;
        }
        return bytes_used;
    }


    public void writeTokensNew(ArrayList<String> tokens) {

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
        int concatenated_list_cursor = 0;
        int inverted_list_cursor = 0;
        int pref, substr_len, freq, collection_freq, inverted_bytes;
        int bytes_for_first_token = bytes_per_frequency + bytes_per_collection_frequency + bytes_per_concatenated_ptr + 1 + bytes_per_inverted_ptr;
        int bytes_for_last_token = bytes_per_frequency + bytes_per_collection_frequency + 1 + bytes_per_inverted_ptr;
        int bytes_for_middle_token = bytes_per_frequency + bytes_per_collection_frequency + 1 + 1 + bytes_per_inverted_ptr;
        int bytes_for_a_whole_eight = bytes_for_first_token + 6 * bytes_for_middle_token + bytes_for_last_token;
        /**
         int num_of_whole_eights = Math.floorDiv(tokens.size(), 8);
         int num_of_spare_tokens = tokens.size() % 8;
         int bytes_for_whole_dict = num_of_whole_eights * bytes_for_a_whole_eight;
        if (num_of_spare_tokens > 0) {
            bytes_for_whole_dict += bytes_for_first_token;
            bytes_for_whole_dict += ((num_of_spare_tokens - 1) * bytes_for_middle_token);
        }
         */
        int whole_eights_in_block = Math.floorDiv(BLOCK, bytes_for_a_whole_eight);
        int size_of_writing_block = whole_eights_in_block * bytes_for_a_whole_eight;
        byte[] toWrite = new byte[size_of_writing_block];
        int offset = 0;
        try {
            this.inverted_index = new BufferedOutputStream(new FileOutputStream(this.dir+INVERTED));
            this.concatenated_list = new BufferedOutputStream(new FileOutputStream(this.dir+CONCATENATED));
            if (need_merge) {
                this.token_review_ordered_in = new BufferedInputStream(new FileInputStream(orderedFileString(ordered_file)));
            }
            else {
                this.token_review_ordered_in = new BufferedInputStream(new FileInputStream(dir + TOKEN_REVIEW));
            }
            this.token_dict = new BufferedOutputStream(new FileOutputStream(dir+TOKENS));
            //int test_cumulative_collection_frequency = 0;
            for (int i = 1; i <= tokens.size(); i++) {
                //System.out.println(i);
                // boolean toPrint = (i<28);

                // saves the token in the concatenated_list, updates the inverted_index, and for each token - saves
                // pointers to those files, and saves size of common prefix with the previous token and some more
                // details.
                String token = tokens.get(i-1);
                if (i % 8 == 1) {  // if first in a block
                    pref = 0;
                }
                else {
                    pref = sizeOfCommonPrefix(token, tokens.get(i-2));
                }
                LinkedHashMap<Integer, Integer> frequencies = readNextTokenReviews(i-1);
                substr_len = this.writeToConcatenatedListNew(token, pref);
                inverted_bytes = this.writeInvertedListNew(frequencies);
                freq = frequencies.size();
                collection_freq = 0;
                for (Integer file_freq : frequencies.values()) {
                    collection_freq += file_freq;
                }
                // writes token details to the token dictionary
                /**if (toPrint) {
                    System.out.println("token: "+token);
                    test_cumulative_collection_frequency += collection_freq;
                    System.out.println(test_cumulative_collection_frequency);
                    for (Map.Entry<Integer,Integer> entry : frequencies.entrySet()) {
                        System.out.println(entry.getKey());
                        System.out.println(entry.getValue());
                    }
                }*/
                offset = generalFunctions.integerToBytesInPlace(freq, toWrite, offset, bytes_per_frequency);
                offset = generalFunctions.integerToBytesInPlace(collection_freq, toWrite, offset, bytes_per_collection_frequency);
                if (i % 8 == 1) {
                    offset = generalFunctions.integerToBytesInPlace(concatenated_list_cursor, toWrite, offset, bytes_per_concatenated_ptr);
                    offset = generalFunctions.integerToBytesInPlace(substr_len, toWrite, offset, 1);
                }
                if (i % 8 == 0) {
                    offset = generalFunctions.integerToBytesInPlace(pref, toWrite, offset, 1);
                }
                if ((i % 8 != 0) & (i % 8 != 1)) {
                    offset = generalFunctions.integerToBytesInPlace(pref, toWrite, offset, 1);
                    offset = generalFunctions.integerToBytesInPlace(substr_len, toWrite, offset, 1);
                }
                offset = generalFunctions.integerToBytesInPlace(inverted_list_cursor, toWrite, offset, bytes_per_inverted_ptr);
                concatenated_list_cursor += substr_len;
                inverted_list_cursor += inverted_bytes;
                if (offset == size_of_writing_block) {
                    token_dict.write(toWrite);
                    offset = 0;
                    if ((tokens.size() - i) < 8 * whole_eights_in_block) {
                        int whole_eights = Math.floorDiv(tokens.size() - i, 8);
                        int spare_tokens = (tokens.size()-i) % 8;
                        int num_bytes = whole_eights * bytes_for_a_whole_eight;
                        if (spare_tokens > 0) {
                            num_bytes += bytes_for_first_token;
                            num_bytes += ((spare_tokens - 1) * bytes_for_middle_token);
                        }
                        size_of_writing_block = num_bytes;
                        toWrite = new byte[size_of_writing_block];
                    }
                }
            }
            inverted_index.close();
            concatenated_list.close();
            token_review_ordered_in.close();
            token_dict.close();
            /**
            BufferedInputStream token_dict_file = new BufferedInputStream(new FileInputStream(dir+TOKENS));
            byte[] test = new byte[100];
            token_dict_file.read(test, 0, 100);
            System.out.println(new String(test, StandardCharsets.UTF_8));
            token_dict_file.close();
             */
            /** TEST CONCAT FILE
            BufferedInputStream test_concat_file = new BufferedInputStream(new FileInputStream(dir+CONCATENATED));
            byte[] test = new byte[100];
            test_concat_file.read(test, 0, 100);
            System.out.println(new String(test, StandardCharsets.UTF_8));
            test_concat_file.close();
             */
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
