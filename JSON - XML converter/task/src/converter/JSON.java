package converter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON extends Converter {
    private static final Pattern JSON_BEGIN = Pattern
            .compile("(?s)^\\s*\\{\\s*[\"}]");
    private static final Pattern OBJECT_BEGIN = Pattern
            .compile("(?s)^\\s*\\{\\s*");
    private static final Pattern OBJECT_END = Pattern
            .compile("(?s)^\\s*}\\s*,?");
    private static final Pattern OBJECT_ATTRIB_NAME = Pattern
            .compile("(?s)^\\s*\"(.*?)\"\\s*:\\s*");
    private static final Pattern OBJECT_ATTRIB_VALUE = Pattern
            .compile("(?s)^\\s*(\"(.*?)\"|(\\d+\\.?\\d*)|(null)),?");
    private static final Pattern PATTERN_EXT_ATTR = Pattern
            .compile("(?i)^[#@][a-z_][.\\w]*");
    private static final Pattern PATTERN_EXT_IDENT = Pattern
            .compile("(?i)^[a-z_][.\\w]*");

    @Override
    protected Element parser(String input) {
        return parsElements(new Bloodhound(input),
                            new Element());
    }

    private Element parsElements(Bloodhound b,
                                 Element parent) {
        if (!b.next(OBJECT_BEGIN)) {
            return null;
        }
        Element e;
        Matcher m;

        while (b.next(OBJECT_ATTRIB_NAME)) {
            e = new Element(b
                           .getMatcher()
                           .group(1));
            if (b.check(JSON_BEGIN)) {
                parsElements(b, e);
                parsObject(e);

            } else if (b.next(
                    OBJECT_ATTRIB_VALUE)) {
                m = b.getMatcher();
                if (m.group(2) != null) {
                    e.setValue(
                            m.group(2));

                } else if (m.group(3) != null) {
                    e.setValue(
                            m.group(3));

                } else if (m.group(4) != null) {
                    e.setValue(null);

                } else {
                    throw new RuntimeException(
                            "Unknown attribute value.");
                }
            } else {
                throw new RuntimeException(
                        "Attribute value expected.");
            }
            parent.addSub(e);
        }

        if (!b.next(OBJECT_END)) {
            throw new RuntimeException(
                    "Object end expected.");
        }

        return parent;
    }

    @Override
    public boolean check(String input) {
        return JSON_BEGIN
                .matcher(input)
                .find();
    }

    private static boolean isValidExtAttributes(String name) {
        return name != null &&
                PATTERN_EXT_ATTR
                        .matcher(name)
                        .matches();
    }

    private static boolean isValidExtIdentifier(String name) {
        return name != null &&
                PATTERN_EXT_IDENT
                        .matcher(name)
                        .matches();
    }

    private static boolean isExtAttributes(Element e) {
        Map<String, Element> subMap = e.getSubMap();
        if (!subMap.containsKey("#" + e.getName())) {
            return false;
        }
        for (Map.Entry<String, Element> subE : subMap.entrySet()) {
            if (!isValidExtAttributes(subE.getKey())) {
                return false;
            }
            if (subE.getKey().charAt(1) == '@'
                    &&
                    subE.getValue().hasSub()) {
                return false;
            }
        }
        return true;
    }

    private static void parsObject(Element parent) {
        Element e;
        if (isExtAttributes(parent)) {
            for (Map.Entry<String, Element> eMap : parent.getSubMap().entrySet()) {
                e = eMap.getValue();
                if (eMap.getKey().charAt(0) == '#') {
                    if (e.hasSub()) {
                        parent.removeSub(e);
                        for (Element subElm: e.getSubMap().values()) {
                            parent.addSub(subElm);
                        }
                    } else {
                        e = parent.removeSub(
                                eMap.getValue());
                        parent.setValue(e.getValue());
                    }
                } else {
                    e = parent.removeSub(eMap.getValue());
                    parent.setAttribute(e.getName().substring(1),
                                        e.getValue());
                }
            }

        } else {
            Map<String, Element> childMap = parent.getSubMap();

            for (Map.Entry<String, Element> eMap : childMap.entrySet()) {
                if (isValidExtAttributes(eMap.getKey())) {
                    if (childMap.containsKey(
                            eMap.getKey().substring(1))) {
                        parent.removeSub(
                                eMap.getValue());
                    } else {
                        eMap.getValue().setName(
                                eMap
                                        .getValue()
                                        .getName()
                                        .substring(1));
                    }
                } else if (!isValidExtIdentifier(
                        eMap.getKey())) {
                    parent.removeSub(
                            eMap.getValue());
                }
            }
            if (!parent.hasSub()) {
                parent.setValue("");
            }
        }
    }

    @Override
    public String print(Element e) {
        return printElement(new StringBuilder(), e)
                       .toString();
    }

    private static StringBuilder printElement(StringBuilder sb,
                                              Element e) {
        if (e.hasAttributes()) {
            sb.append("{\n");

            for (Map.Entry<String, String> eMap: e
                                                 .getAttrMap()
                                                 .entrySet()) {
                sb.append(String.format(
                        "\"@%s\" : \"%s\",\n",
                        eMap.getKey(),
                        eMap.getValue()));
            }

            if (e.hasSub()) {
                sb.append(String.format(
                        "\"#%s\": ",
                        e.getName()));
                printSub(sb, e);
            } else if (e.getValue() == null) {
                sb.append(String.format(
                        "\"#%s\" : null\n",
                        e.getName()));
            } else {
                sb.append(String.format(
                        "\"#%s\" : \"%s\"\n",
                        e.getName(),
                        e.getValue()));
            }
            sb.append(" }");
        } else if (e.hasSub()) {
            printSub(sb, e);
        } else if (e.getValue() == null) {
            sb.append("null");
        } else {
            sb.append(String.format(
                    "\"%s\"",
                    e.getValue()));
        }
        return sb;
    }

    private static void printSub(StringBuilder sb,
                                 Element parent) {
        if (parent.hasSub()) {
            sb.append("{\n");
        }
        Element e;
        for (int i = 0; i < parent
                              .getSubElList()
                              .size();
             i++) {
            e = parent
                    .getSubElList()
                    .get(i);
            sb.append(String.format(
                    "\"%s\" : ",
                    e.getName()));
            printElement(sb, e);

            if (i != parent
                    .getSubElList()
                    .size() - 1) {
                sb.append(",\n");
            }
        }
        if (parent.hasSub()) {
            sb.append(" }");
        }
    }
}