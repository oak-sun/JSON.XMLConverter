package converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) throws IOException {

        var json = Files
                              .readString(Path.of("test.txt"))
                              .replace(",", "~")
                              .replaceAll("\\{", "~{~")
                              .replaceAll("\\s*}", "~}~")
                              .split("~");

        for (int i = 0; i < json.length; i++) {
            if (json[i].matches("\\{") &&
                    json[i + 1].contains("@")) {

                var child1 = new HashMap<String, Integer>();
                var child2 = new HashMap<String, Integer>();

                for (int k = i + 1; !json[k].matches("}"); k++) {
                    if (json[k].matches("[@#]"))
                        child1.put(json[k]
                                        .split(":")[0], k);
                    if (!json[k].matches("[{}]"))
                        child2.put(json[k]
                                .split(":")[0], k);
                    child1
                            .entrySet()
                            .stream()
                            .filter(e -> child2
                                                 .entrySet()
                                                 .stream()
                                                .anyMatch(s -> e
                                                                .getKey()
                                                                .matches(s.getKey())))
                            .forEach(e -> json[e.getValue()] = "");
                }
            }
        }
        for (int i = 0; i < json.length; i++) {
            if (json[i].matches("}")) {
                var res = 0;
                var internal = 0;
                for (int k = i - 1; ; k--) {
                    if (json[k].matches("(@[^\"]+)|#"))
                        internal++;
                    else if (json[k].matches("\\{") &&
                            res != internal) {

                        for (int s = k - 1; !json[s]
                                          .matches("\\{"); s--) {
                            json[s] = json[s]
                                           .replaceAll("[@#]", "");
                            if (json[s]
                                    .split(":")[0]
                                    .length() == 2)
                                json[s] = "";
                        }
                        break;
                    }
                    res++;
                }
            }
        }
        var paths = new ArrayDeque<String>();
        for (int i = 0; i < json.length; i++) {
            if (!paths.isEmpty() &&
                    json[i].matches("}"))
                paths.removeLast();
            else if (json[i].contains("#") &&
                    i - 1 > 0)
                json[i] = json[i]
                          .replace("#", "");
            else if (json[i]
                    .matches("\\s*\"[^@#]+\":.+")) {
                var path = json[i]
                        .replace(":", "")
                        .replaceAll("\\s", "");

                if (path.matches("\"[^@#\"]+\""))
                    paths.add(path
                            .replace("\"", ""));

                if (json[i + 1].matches("\\{")) {
                    if (json[i + 2].matches("[@#].+")) {
                        for (int k = i + 2; json[k].contains("@"); k++) {
                            print(String.format(
                                    "%s = \n",
                                    json[i].split(":")[0]),
                                    paths, json[i].split(":")[1]);
                        }
                    } else print("", paths, "");
                } else {
                    var split = json[i].split("\\s*:\\s*");
                    paths.add(split[0]
                                    .replace(":", "")
                                    .replaceAll("\\s", "")
                                    .replace("\"", ""));
                    print("",
                            paths,
                            "null".equals(split[1]) ?
                                    "null" :
                                    "\"" + split[1]
                                            .replaceAll("\"", "") + "\"");
                    paths.removeLast();
                }
            }
        }
    }

    static void print(String attributes,
                      Deque<String> path,
                      String value) {
        System.out.printf(
                "Element:\npath = %s\n%s\n%s\n",
                path.isEmpty() ?
                        "" :
                        String.join(", ", path),
                        value.isBlank() ?
                                 "" :
                                "value = " + value,
                                           attributes.isBlank() ?
                                "" : "attributes:\n" + attributes);
    }
}