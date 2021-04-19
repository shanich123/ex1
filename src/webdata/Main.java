package webdata;
import java.io.*;
import java.io.FileReader;
import java.util.*;
import java.io.FileOutputStream;

public class Main {

    public static void main(String[] args) {
        SlowIndexWriter a = new SlowIndexWriter();
        a.slowWrite("1000.txt", "try");
//        a.removeIndex("try");
        String inputString = "Hello World!";
        byte[] byteArrray = inputString.getBytes();



    }
}
