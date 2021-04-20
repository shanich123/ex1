package webdata;

public class Product {

    private String productId;
    private int firstReviewId;

    public Product (String pId, int reviewId){
        this.productId = pId;
        this.firstReviewId = reviewId;
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
}
