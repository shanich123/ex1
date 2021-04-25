package webdata;

public class Review {

    private int score, helpfulnessNumerator, helpfulnessDenominator, reviewLength, productNum;
    private String productID;

    public Review(int score, int helpfulnessNumerator, int helpfulnessDenominator, int reviewLength, String curProductId){
        this.productID = curProductId;
        this.score = score;
        this.helpfulnessNumerator = helpfulnessNumerator;
        this.helpfulnessDenominator = helpfulnessDenominator;
        this.reviewLength = reviewLength;
        this.productNum = -1;
    }

    public int getHelpfulnessDenominator() {
        return helpfulnessDenominator;
    }

    public int getHelpfulnessNumerator() {
        return helpfulnessNumerator;
    }

    public int getScore() {
        return score;
    }

    public int getReviewLength() {
        return reviewLength;
    }

    public void setProductNum(int productNum) {
        this.productNum = productNum;
    }

    public int getProductNum() {
        return productNum;
    }

    public String getProductID() {
        return productID;
    }

    @Override
    public String toString() {
        return "Review{" +
                "score=" + score +
                ", helpfulnessNumerator=" + helpfulnessNumerator +
                ", helpfulnessDenominator=" + helpfulnessDenominator +
                ", ReviewLength=" + reviewLength +
                ", productID=" + productID +
                '}';
    }
}
