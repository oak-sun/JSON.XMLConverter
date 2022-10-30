package converter;

import converter.model.JSON_metamorph;
import converter.model.XML_metamorph;

public class Main {
    public static void main(String[] args)
                                      throws java.io.IOException {
        var input = java
                .nio
                .file
                .Files
                .readAllLines(java
                        .nio
                        .file
                        .Paths
                        .get("test.txt"))
                .stream()
                .map(String::trim)
                .collect(java
                        .util
                        .stream
                        .Collectors
                        .joining(""));
        System.out.println(input
                .matches("^\\s*<.+") ?
                new JSON_metamorph(input).output()
                :
                new XML_metamorph(input).output());
    }
}
