package webdata;

public class TokenReview implements Comparable<TokenReview>{

    private int token, review;

    public TokenReview(int token, int review){
        this.token = token;
        this.review = review;
    }

    public int getToken() {
        return token;
    }

    public int getReview() {
        return review;
    }

    @Override
    public int compareTo(TokenReview o) {
        if (this.token == o.token){
            if (this.review > o.review){
                return 1;
            }
            else if (this.review < o.review){
                return -1;
            }
            return 0;
        }
        else if (this.token > o.token){
            return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "TokenReview{" +
                "token=" + token +
                ", review=" + review +
                '}';
    }
}
