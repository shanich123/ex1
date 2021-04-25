package webdata;

import java.util.Enumeration;

public class webEnum implements Enumeration<Integer> {

    private Integer first, last;

    public webEnum (int first, int last){
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


