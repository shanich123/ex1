package webdata;
import java.util.ArrayList;

public class Token implements Comparable<Token> {

    private String word;
    private int frequency;
    private int collectionFrequency; // we assume that the number is bounded in int
    private ArrayList<Integer> invertedIndex;
    private ArrayList<Integer> invertedFrequency;
    private int prefix_size;
    private int concatenated_list_ptr, inverted_list_ptr;

    public Token(String word, int reviewId){
        this.word = word;
        this.frequency = 0;
        this.collectionFrequency = 0;
        this.invertedIndex = new ArrayList<Integer>();
        this.invertedFrequency = new ArrayList<Integer>();
        this.prefix_size = 0;
        this.concatenated_list_ptr = 0;
        this.inverted_list_ptr = 0;
        this.setFrequency(reviewId);
    }

    public void setPrefixSize(int pref) {
        this.prefix_size = pref;
    }

    public void setConcatenatedPtr(int cursor) {
        this.concatenated_list_ptr = cursor;
    }

    public void setInvertedPtr(int cursor) {
        this.inverted_list_ptr = cursor;
    }

    // update that the token appears in the given reviewId
    public void setFrequency(int reviewId) {
        int cur;
        int pos = this.invertedIndex.indexOf(reviewId);
        if (pos == -1) {
            this.frequency += 1;
            this.invertedIndex.add(reviewId);
            this.invertedFrequency.add(1);
        }
        else {
            cur = this.invertedFrequency.get(pos);
            this.invertedFrequency.set(pos, cur + 1);
        }
        this.collectionFrequency += 1;
    }

    public int getCollectionFrequency() {
        return collectionFrequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getWord() {
        return word;
    }

    public ArrayList<Integer> getInvertedIndex() {
        return invertedIndex;
    }

    public ArrayList<Integer> getInvertedFrequency() {
        return invertedFrequency;
    }

    public int getPrefixSize() {
        return this.prefix_size;
    }

    public int getConcatenatedPtr() {
        return this.concatenated_list_ptr;
    }

    public int getInvertedPtr() {
        return this.inverted_list_ptr;
    }

    @Override
    public int compareTo (Token other){
        return this.word.compareTo(other.getWord());
    }

    @Override
    public String toString() {
        return "Token{" +
                "word='" + word + '\'' +
                ", frequency=" + frequency +
                ", collectionFrequency=" + collectionFrequency +
                ", invertedIndex=" + invertedIndex + ", invertedFrequency=" + invertedFrequency +
                '}';
    }
}
