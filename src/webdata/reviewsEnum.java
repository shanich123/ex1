package webdata;

import java.util.Enumeration;

/**
 * This class is an enumerator that iterates over the numbers from first to last (including).
 */
public class reviewsEnum implements Enumeration<Integer> {

    private Integer first, last;

    public reviewsEnum (int first, int last){
        this.first = first;
        this.last = last;
    }

    @Override
    public boolean hasMoreElements() {
        if (this.first<= this.last){
            return true;
        }
        return false;
    }

    @Override
    public Integer nextElement() {
        if (hasMoreElements()){
            int toReturn = this.first;
            this.first += 1;
            return toReturn;
        }
        return null;
    }
}


