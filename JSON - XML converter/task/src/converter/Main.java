package converter;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

      var file = new File("test.txt");
        var sb = new StringBuilder();
        try ( var sc = new Scanner(file) ) {
            while (sc.hasNext())
                sb.append(sc.nextLine());

        } catch (Exception e) {
            e.printStackTrace();
        }

        var input = sb
                            .toString()
                            .replaceAll("\\s+", " ");

        var dealer = new MetamorphosisDealer(input);
        System.out.println(dealer);
    }
}