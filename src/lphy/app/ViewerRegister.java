package lphy.app;

import lphy.app.treecomponent.TimeTreeComponent;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.swing.BooleanValueEditor;
import lphy.graphicalModel.swing.DoubleValueEditor;
import lphy.graphicalModel.swing.IntegerValueEditor;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import java.awt.*;

/**
 * This class registers viewers for different classes of values
 */
public class ViewerRegister {

    private static Viewer primitiveArrayViewer = new Viewer() {
        public boolean match(Object object) {
            if (object instanceof Value) {
                Object value = ((Value) object).value();
                return (value instanceof Double[] || value instanceof Number[] || value instanceof Integer[] || value instanceof Boolean[] || value instanceof String[]);
            }
            return false;
        }

        public JComponent getViewer(Object object) {

            Object value = ((Value) object).value();

            if (value instanceof Double[]) {
                return new DoubleArrayLabel((Value<Double[]>) object);
            }

            if (value instanceof Number[]) {
                return new NumberArrayLabel((Value<Number[]>) object);
            }

            if (value instanceof String[]) {
                return new StringArrayLabel((Value<String[]>) object);
            }

            if (value instanceof Integer[] || value instanceof Boolean[]) {
                return new ArrayLabel((Value) object);
            }

            throw new IllegalArgumentException("Unexpected argument: " + object);
        }
    };

    private static Viewer doubleValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Double;

            } else return false;
        }

        public JComponent getViewer(Object object) {
            Value value = (Value) object;
            if (value.getGenerator() == null) {
                return new DoubleValueEditor(value);
            } else {
                return new JLabel(value.value().toString());
            }
        }
    };

    private static Viewer doubleArray2DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Double[][];

            } else return object instanceof Double[][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Double[][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Double[][]) value.value();
            } else {
                rawValue = (Double[][]) object;
            }

            return new DoubleArray2DEditor(rawValue, editable);
        }
    };

    private static Viewer integerValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Integer;

            } else return false;
        }

        public JComponent getViewer(Object object) {
            Value value = (Value) object;
            if (value.getGenerator() == null) {
                return new IntegerValueEditor(value);
            } else {
                return new JLabel(value.value().toString());
            }
        }
    };

    private static Viewer booleanValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Boolean && value.getGenerator() == null;

            } else return false;
        }

        public JComponent getViewer(Object value) {
            return new BooleanValueEditor((Value) value);
        }
    };


    private static Viewer alignmentValueViewer = new Viewer() {

        @Override
        public boolean match(Object value) {
            return value instanceof Alignment || (value instanceof Value && ((Value) value).value() instanceof Alignment);
        }

        @Override
        public JComponent getViewer(Object value) {
            if (value instanceof Alignment) {
                return new AlignmentComponent(new Value(null, value));
            }
            return new AlignmentComponent((Value<Alignment>) value);
        }
    };

    private static Viewer taxaValueViewer = new Viewer() {

        @Override
        public boolean match(Object value) {
            return value instanceof Taxa || (value instanceof Value && ((Value) value).value() instanceof Taxa);
        }

        @Override
        public JComponent getViewer(Object value) {
            if (value instanceof Taxa) {
                return new TaxaComponent((Taxa)value);
            }
            return new TaxaComponent(((Value<Taxa>) value).value());
        }
    };

    private static Viewer timeTreeValueViewer = new Viewer() {

        @Override
        public boolean match(Object object) {
            return object instanceof TimeTree || (object instanceof Value && ((Value) object).value() instanceof TimeTree);
        }

        @Override
        public JComponent getViewer(Object object) {
            if (object instanceof TimeTree) {
                return new TimeTreeComponent((TimeTree) object);
            }
            return new TimeTreeComponent(((Value<TimeTree>) object).value());
        }
    };

    public static Viewer[] viewers = {
            doubleValueViewer,
            integerValueViewer,
            booleanValueViewer,
            doubleArray2DViewer,
            alignmentValueViewer,
            timeTreeValueViewer,
            taxaValueViewer,
            primitiveArrayViewer,
            new VectorValueViewer()
    };

    private static Viewer getViewerForValue(Object object) {
        for (Viewer viewer : viewers) {
            if (viewer.match(object)) return viewer;
        }
        LoggerUtils.log.severe("Found now viewer for " + object);
        return null;
    }

    public static JComponent getJComponentForValue(Object object) {
        for (Viewer viewer : viewers) {
            if (viewer.match(object)) return viewer.getViewer(object);
        }
        LoggerUtils.log.severe("Found now viewer for " + object);
        String label;
        if (object instanceof Value) {
            label = ((Value) object).getLabel();
        } else {
            label = object.toString();
        }
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.red);
        return jLabel;
    }
}
