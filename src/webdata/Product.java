package webdata;

public class Product implements Comparable<Product>{

    private String productId;
    private int firstReviewId, lastReviewId;

    public Product (String pId, int reviewId){
        this.productId = pId;
        this.firstReviewId = reviewId;
        this.lastReviewId = -1;
    }

    public void setLastReviewId(int lastReviewId) {
        this.lastReviewId = lastReviewId;
    }

    public int getLastReviewId() {
        return lastReviewId;
    }

    public int getFirstReviewId() {
        return this.firstReviewId;
    }

    public String getProductId() {
        return this.productId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", firstReviewId=" + firstReviewId +
                '}';
    }

    @Override
    public int compareTo (Product other){
        return this.productId.compareTo(other.getProductId());
    }
}
