package converter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Element {
    private String name;
    private String value;

    private final Map<String, String> attrMap =
            new LinkedHashMap<>();
    private final List<Element> subElList =
            new ArrayList<>();

    public Element() {
        this(null);
    }

    public Element(String name) {
        this.name = name;
    }

    public void setAttribute(String key,
                             String value) {
        attrMap.put(key, value);
    }

    public boolean hasAttributes() {
        return !attrMap.isEmpty();
    }



    public Element addSub(String name) {
        return addSub(new Element(name));
    }

    public Element addSub(Element subElement) {
        subElList.add(subElement);
        return subElement;
    }

    public Element removeSub(Element e) {
        var i = subElList.indexOf(e);
        if (i < 0) {
            return null;
        }
        return subElList.remove(i);
    }

    public boolean hasSub() {
        return !subElList.isEmpty();
    }

    public Map<String, Element> getSubMap() {
        Map<String, Element> map = new LinkedHashMap<>();
        for (Element e : subElList) {
            map.put(e.getName(), e);
        }
        return map;
    }

}
