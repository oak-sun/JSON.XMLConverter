package converter;

public abstract class Converter {

    private Element root;

    public Element getRoot() {
        return root;
    }

    public void parsData(String input) {
        root = parser(input.trim());
    }

    protected abstract Element parser(String input);

    protected abstract boolean check(String src);

    protected abstract String print(Element element);
}
