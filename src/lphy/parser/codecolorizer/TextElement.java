package lphy.parser.codecolorizer;

import javax.swing.text.Style;
import java.util.ArrayList;
import java.util.List;

public class TextElement {

    List<String> text = new ArrayList<>();
    List<Style> style = new ArrayList<>();

    public TextElement() {
    }

    public TextElement(String text, Style style) {
        add(text, style);
    }

    void add(String text, Style style) {
        this.text.add(text);
        this.style.add(style);
    }

    void add(TextElement e) {
        for (int i = 0; i < e.text.size(); i++) {
            add(e.text.get(i), e.style.get(i));
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String t : text) {
            builder.append(t);
        }
        return builder.toString();
    }
}

