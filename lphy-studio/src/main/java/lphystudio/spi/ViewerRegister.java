package lphystudio.spi;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.ContinuousCharacterData;
import lphy.base.evolution.tree.TimeTree;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.Value;
import lphy.core.model.datatype.MapValue;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.graphicalmodelpanel.MethodInfoPanel;
import lphystudio.app.graphicalmodelpanel.VectorValueViewer;
import lphystudio.app.graphicalmodelpanel.viewer.*;
import lphystudio.app.treecomponent.TimeTreeComponent;
import lphystudio.core.valueeditor.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class registers viewers for different classes of values
 */
public class ViewerRegister {

    static ServiceLoader loader;

    /**
     * the list of {@link Viewer}, containing those loaded by SPI from other lphy extensions.
     */
    static List<Viewer> viewerList = new ArrayList<>();


    /**
     * declare all viewers below
     */

    private static Viewer methodInfoViewer = new Viewer() {
        @Override
        public boolean match(Object value) {
            return (value instanceof Value && MethodInfoPanel.hasZeroParamMethodInfo((Value) value));
        }

        @Override
        public JComponent getViewer(Object value) {
            return new MethodInfoPanel((Value) value);
        }

        @Override
        public String toString() {
            return "Method Info Viewer";
        }
    };

    private static Viewer sequenceTypeValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof SequenceType;

            } else return (object instanceof SequenceType);
        }

        public JComponent getViewer(Object object) {

            SequenceType sequenceType = null;

            if (object instanceof Value) {
                Value value = (Value) object;
                sequenceType = ((Value<SequenceType>)value).value();
            } else sequenceType = (SequenceType)object;

            StringBuilder builder = new StringBuilder();
            builder.append(sequenceType.getName());
            builder.append(": ");
            for (State state : sequenceType.getCanonicalStates()) {
                builder.append(state);
                builder.append(" ");
            }

            return new JLabel(builder.toString());
        }

        @Override
        public String toString() {
            return "Sequence Type Viewer";
        }
    };

    private static Viewer primitiveArrayViewer = new Viewer() {
        public boolean match(Object object) {
            if (object instanceof Value) {
                Object value = ((Value) object).value();
                return (value instanceof Double[] || value instanceof Number[] ||
                        value instanceof Boolean[] || value instanceof String[] ||
                        value instanceof Object[]);
            } else
                return (object instanceof Double[] || object instanceof Number[] ||
                        object instanceof Boolean[] || object instanceof String[]);
        }

        public JComponent getViewer(Object object) {

            Object array;

            if (object instanceof Value) {
                array = ((Value) object).value();
            } else {
                array = object;
            }

            if (array instanceof Double[]) {
                return new DoubleArrayLabel((Double[]) array);
            }

            if (array instanceof Number[]) {
                return new NumberArrayLabel((Number[]) array);
            }

            if (array instanceof String[]) {
                return new StringArrayLabel((String[]) array);
            }

            if (array instanceof Boolean[]) {
                return new ArrayLabel((Boolean[]) array);
            }

            if (array instanceof Object[]) {
                return new ArrayLabel((Object[]) array);
            }
            throw new IllegalArgumentException("Unexpected argument: " + object);
        }

        @Override
        public String toString() {
            return "Primitive Array Viewer";
        }
    };

    private static Viewer doubleValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Double;

            } else return (object instanceof Double);
        }

        public JComponent getViewer(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                if (value.getGenerator() == null) {
                    return new DoubleValueEditor(value);
                } else {
                    return new JLabel(value.value().toString());
                }
            } else return new JLabel(object.toString());
        }

        @Override
        public String toString() {
            return "Double value Viewer";
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

        @Override
        public String toString() {
            return "Double 2d array Viewer";
        }
    };

    private static Viewer doubleArray3DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Double[][][];

            } else return object instanceof Double[][][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Double[][][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Double[][][]) value.value();
            } else {
                rawValue = (Double[][][]) object;
            }

            return new DoubleArray3DEditor(rawValue, editable);
        }

        @Override
        public String toString() {
            return "Double 3d array Viewer";
        }
    };

    private static Viewer integerValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Integer;

            } else return object instanceof Integer;
        }

        public JComponent getViewer(Object object) {
            if (object instanceof Value) {
                Value value = (Value) object;
                if (value.getGenerator() == null) {
                    return new IntegerValueEditor(value);
                } else {
                    return new JLabel(value.value().toString());
                }
            } else return new JLabel(object.toString());
        }

        @Override
        public String toString() {
            return "Integer value Viewer";
        }
    };

    private static Viewer integerArray2DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Integer[][];

            } else return object instanceof Integer[][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Integer[][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Integer[][]) value.value();
            } else {
                rawValue = (Integer[][]) object;
            }

            return new IntegerArray2DEditor(rawValue, editable);
        }

        @Override
        public String toString() {
            return "Integer 2d array Viewer";
        }
    };

    private static Viewer intArray3DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Integer[][][];

            } else return object instanceof Integer[][][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Integer[][][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Integer[][][]) value.value();
            } else {
                rawValue = (Integer[][][]) object;
            }

            return new IntegerArray3DEditor(rawValue, editable);
        }

        @Override
        public String toString() {
            return "Integer 3d array Viewer";
        }
    };


    private static Viewer booleanArray2DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Boolean[][];

            } else return object instanceof Boolean[][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Boolean[][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Boolean[][]) value.value();
            } else {
                rawValue = (Boolean[][]) object;
            }

            return new BooleanArray2DEditor(rawValue, editable);
        }

        @Override
        public String toString() {
            return "Boolean 2d array Viewer";
        }
    };

    private static Viewer booleanArray3DViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Boolean[][][];

            } else return object instanceof Boolean[][][];
        }

        public JComponent getViewer(Object object) {
            boolean editable = false;
            Boolean[][][] rawValue;
            if (object instanceof Value) {
                Value value = (Value) object;
                editable = value.getGenerator() == null;
                rawValue = (Boolean[][][]) value.value();
            } else {
                rawValue = (Boolean[][][]) object;
            }

            return new BooleanArray3DEditor(rawValue, editable);
        }

        @Override
        public String toString() {
            return "Boolean 3d array Viewer";
        }
    };

    private static Viewer stringValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof String;

            } else return object instanceof String;
        }

        public JComponent getViewer(Object object) {
            if (object instanceof Value) {
                Value<String> value = (Value<String>) object;
                if (value.getGenerator() == null) {
                    return new StringValueEditor(value);
                } else {
                    return new JLabel(value.value());
                }
            } else {
                return new JLabel("\"" + object.toString()+"\"");
            }
        }

        @Override
        public String toString() {
            return "String value Viewer";
        }
    };

    private static Viewer booleanValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value) {
                Value value = (Value) object;
                return value.value() instanceof Boolean;

            } else return false;
        }

        public JComponent getViewer(Object object) {
            Value value = (Value) object;

            if (value.getGenerator() == null) {
                return new BooleanValueEditor(value);
            } else {
                return new JLabel(value.value().toString());
            }
        }

        @Override
        public String toString() {
            return "Boolean value Viewer";
        }
    };

    private static Viewer listInValueViewer = new Viewer() {

        public boolean match(Object object) {

            if (object instanceof Value value) {
                return value.value() instanceof List<?>;
            } else return false;
        }

        public JComponent getViewer(Object object) {

            if (object instanceof Value value) {
                List<?> list = (List<?>) Objects.requireNonNull(value).value();
                String[] strArr = list.stream().map(Object::toString).toArray(String[]::new);
                return new StringArrayLabel(Objects.requireNonNull(strArr));
            } else return new JLabel(object.toString());
        }

        @Override
        public String toString() {
            return "List Viewer";
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
        @Override
        public String toString() {
            return "Alignment Viewer";
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
                return new TaxaComponent((Taxa) value);
            }
            return new TaxaComponent(((Value<Taxa>) value).value());
        }

        @Override
        public String toString() {
            return "Taxa Viewer";
        }
    };

    private static Viewer continuousCharacterDataViewer = new Viewer() {

        @Override
        public boolean match(Object value) {
            return value instanceof ContinuousCharacterData || (value instanceof Value && ((Value) value).value() instanceof ContinuousCharacterData);
        }

        @Override
        public JComponent getViewer(Object value) {
            if (value instanceof Taxa) {
                return new ContinuousCharacterDataComponent((ContinuousCharacterData) value);
            }
            return new ContinuousCharacterDataComponent(((Value<ContinuousCharacterData>) value).value());
        }

        @Override
        public String toString() {
            return "Continuous Character Viewer";
        }
    };

    private static Viewer mapValueViewer = new Viewer() {

        @Override
        public boolean match(Object value) {
            return value instanceof MapValue;
        }

        @Override
        public JComponent getViewer(Object value) {
            return new MapComponent((MapValue) value);
        }

        @Override
        public String toString() {
            return "Map Value Viewer";
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

        @Override
        public String toString() {
            return "Time Tree Viewer";
        }
    };

    public static Viewer[] viewers = {
            doubleValueViewer,
            integerValueViewer,
            stringValueViewer,
            booleanValueViewer,
            doubleArray2DViewer,
            doubleArray3DViewer,
            integerArray2DViewer,
            intArray3DViewer,
            booleanArray2DViewer,
            booleanArray3DViewer,
            mapValueViewer,
            alignmentValueViewer,
            timeTreeValueViewer,
            taxaValueViewer,
            continuousCharacterDataViewer,
            primitiveArrayViewer,
            listInValueViewer,
            new VectorValueViewer(),
            methodInfoViewer,
            sequenceTypeValueViewer
    };

//        private static Viewer getViewerForValue(Object object) {
//            for (Viewer viewer : viewers) {
//                if (viewer.match(object)) return viewer;
//            }
//            LoggerUtils.log.severe("Found no viewer for " + object);
//            return null;
//        }

    static {
        loader = ServiceLoader.load(Viewer.class);


        //*** Viewer must have a public no-args constructor ***//
        Iterator<Viewer> viewerIterator = loader.iterator();

        while (viewerIterator.hasNext()) {
            Viewer viewer = null;
            try {
                //*** Viewer must have a public no-args constructor ***//
                viewer = viewerIterator.next();
            } catch (ServiceConfigurationError serviceError) {
                LoggerUtils.log.severe(serviceError.getMessage());
                serviceError.printStackTrace();
            }

            // TODO validation here?
            viewerList.add(viewer);
        }

        // register all viewers in studio core
        viewerList.addAll(Arrays.stream(viewers).toList());


    }

    /**
     * Call this in the panel to show the corresponding viewer given a value.
     * @param object  a value
     * @return        the corresponding viewer
     */
    public static JComponent getJComponentForValue(Object object) {
        // loop through all viewers.
        for (Viewer viewer : viewerList) {
            if (viewer.match(object))
                return viewer.getViewer(object);
        }
//            LoggerUtils.log.severe("Found no viewer for " + object);
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
