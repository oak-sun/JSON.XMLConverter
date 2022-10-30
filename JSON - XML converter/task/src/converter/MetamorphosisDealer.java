package converter;

public class MetamorphosisDealer {

    String input;
    Converter convert;
    Converter print;

    public MetamorphosisDealer(String input) {
        this.input = input.trim();
        setMethod();
        this.convert
                .parsData(input);
    }

    @Override
    public String toString() {
        return print.print(
                convert.getRoot());
    }

    public void setMethod() {
        var converters = new Converter[] {new XML(), new JSON()};
        if (converters[0].check(input)) {
            convert = converters[0];
            print = converters[1];
        } else {
            convert = converters[1];
            print = converters[0];
        }
    }
}
