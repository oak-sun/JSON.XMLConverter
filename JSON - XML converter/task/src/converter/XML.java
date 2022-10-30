package converter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XML extends Converter {
    private static final Pattern XML_BEGIN = Pattern
                    .compile("(?s)\\A\\s*<\\s*[a-z_]\\w+");
    private static final Pattern TAG_OPEN = Pattern
                    .compile("(?is)^\\s*<\\s*([a-z_]\\w+)" +
                            "\\s*([a-z_]\\w+\\s*=\\s*\".*?\")*\\s*" +
                            "(>|/>)");
    private static final Pattern ATTRIBUTES = Pattern
            .compile("(?is)([a-z_]\\w+)\\s*=\\s*\"(.*?)\"");

    private static final Pattern UNNECESSARY_TAG = Pattern
                    .compile("<\\?[\\s\"\\w-.=]*\\?>");

    @Override
    public boolean check(String input) {
        return XML_BEGIN
                .matcher(input)
                .find()
                ||
                UNNECESSARY_TAG
                        .matcher(input)
                        .find();
    }

    @Override
    protected Element parser(String input) {
        var bloodhound = new Bloodhound(input);
        if (bloodhound.check(UNNECESSARY_TAG)) {
            System.out.println("UNNECESSARY_TAG");
        }
        return parsElements(bloodhound,
                           new Element());
    }

    private Element parsElements(Bloodhound b,
                                 Element parent) {
        Element e;
        Matcher m;
        while (b.next(TAG_OPEN)) {
            m = b.getMatcher();
            e = parent.addSub(m.group(1));
            parsAttributes(m.group(2), e);

            if (">".equals(m.group(3))) {
                if (b.check(XML_BEGIN)) {
                    parsElements(b, e);
                }
                if (!b.next(
                        String.format(
                                "(?s)^(.*?)<\\s*\\/%s\\s*>",
                                 e.getName()))) {
                    throw new RuntimeException(
                            "Enclosing tag expected.");
                }
                if (!e.hasSub()) {
                    e.setValue(b
                               .getMatcher()
                               .group(1));
                }
            }
        }
        return parent;
    }

    private static void parsAttributes(String src,
                                       Element e) {
        if (src == null) {
            return;
        }
        Matcher m;
        var f = new Bloodhound(src);
        while (f.next(ATTRIBUTES)) {
            m = f.getMatcher();
            e.setAttribute(m.group(1),
                           m.group(2));
        }
    }

    @Override
    public String print(Element e) {
        return printElement(new StringBuilder(), e)
                         .toString();
    }

    private static StringBuilder printElement(StringBuilder sb,
                                              Element e) {
        var eName = e.getName();
        if (eName == null) {
            if (e.getSubElList().size() > 1) {
                sb.append("<root>\n");
            }
            for (Element sub : e.getSubElList()) {
                printElement(sb, sub);
            }
            if (e.getSubElList().size() > 1) {
                sb.append("</root>\n");
            }
            return sb;
        }
        sb.append(String.format(
                "<%s", eName));
        for (Map.Entry<String, String> eMap: e
                                            .getAttrMap()
                                            .entrySet()) {
            sb.append(String.format(
                    " %s = \"%s\"",
                    eMap.getKey(),
                    eMap.getValue() == null ?
                            "" : eMap.getValue()));
        }
        if (e.hasSub()) {
            sb.append(">\n");
            for (Element sub : e.getSubElList()) {
                printElement(sb, sub);
            }
            sb.append(String.format(
                    "</%s>\n", eName));
        } else if (e.getValue() == null) {
            sb.append("/>\n");
        } else {
            sb.append(">");
            sb.append(e.getValue());
            sb.append(
                    String.format("</%s>\n", eName));
        }
        return sb;
    }
}
