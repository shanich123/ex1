package webdata;

public class Main {

    public static void main(String[] args) {
        SlowIndexWriter a = new SlowIndexWriter();
        a.slowWrite("1000.txt", "try");
        a.removeIndex("try");
    }
}
