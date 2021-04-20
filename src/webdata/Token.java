package webdata;
import java.util.ArrayList;

public class Token implements Comparable<Token> {

    private String word;
    private int frequency;
    private int collectionFrequency; // we assume that the number is bounded in int
    private ArrayList<Integer> invertedIndex;

    public Token(String word, int tokenFrequency, int tokenCollectionFrequency, int reviewId){
        this.word = word;
        this.frequency = tokenFrequency;
        this.collectionFrequency = tokenCollectionFrequency;
        this.invertedIndex = new ArrayList<Integer>();
        this.invertedIndex.add(reviewId);
    }

    public void setFrequency(int reviewId) {
        if (!invertedIndex.contains(reviewId)) {
            this.frequency += 1;
            invertedIndex.add(reviewId);
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
                ", invertedIndex=" + invertedIndex +
                '}';
    }
}
