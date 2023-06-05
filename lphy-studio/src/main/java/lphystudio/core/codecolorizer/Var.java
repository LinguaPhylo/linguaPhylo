package lphystudio.core.codecolorizer;

import lphy.core.model.GraphicalModel;
import lphy.core.model.component.RandomVariable;
import lphy.core.model.component.Value;

class Var {
    CodeColorizer codeColorizer;
    String id;
    TextElement rangeList;

    public Var(CodeColorizer codeColorizer, String id, TextElement rangeList) {
        this.codeColorizer = codeColorizer;
        this.id = id;
        this.rangeList = rangeList;
    }

    public boolean isRangedVar() {
        return rangeList != null;
    }

    public String getId() {
        return id;
    }

    public TextElement getTextElement(GraphicalModel model, GraphicalModel.Context context) {
        TextElement element = getIDElement(id, model, context);
        if (isRangedVar()) {
            element.add(new TextElement("[", codeColorizer.getStyle(CodeColorizer.ElementType.punctuation)));
            element.add(rangeList);
            element.add(new TextElement("]", codeColorizer.getStyle(CodeColorizer.ElementType.punctuation)));
        }
        return element;
    }

    private TextElement getIDElement(String id, GraphicalModel model, GraphicalModel.Context context) {
        TextElement element;
        if (model.hasValue(id, context)) {
            Value value = model.getValue(id, context);
            if (model.isClamped(id)) // data clamping
                element = new TextElement(id, codeColorizer.getStyle(CodeColorizer.ElementType.clampedVar));
            else
                element = new TextElement(id, value instanceof RandomVariable ? codeColorizer.getStyle(CodeColorizer.ElementType.randomVariable) : codeColorizer.getStyle(CodeColorizer.ElementType.value));
        } else {
            element = new TextElement(id, codeColorizer.getStyle(CodeColorizer.ElementType.literal));
        }
        return element;
    }

}
