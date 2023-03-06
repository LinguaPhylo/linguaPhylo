package lphystudio.app.graphicalmodelpanel;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.MapValue;
import lphy.util.LoggerUtils;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.graphicalmodelpanel.viewer.*;
import lphystudio.app.treecomponent.TimeTreeComponent;
import lphystudio.core.valueeditors.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * This class registers viewers for different classes of values
 */
public class ViewerRegister {

    private static Viewer methodInfoViewer = new Viewer() {
        @Override
        public boolean match(Object value) {
            return (value instanceof Value && MethodInfoPanel.hasZeroParamMethodInfo((Value) value));
        }

        @Override
        public JComponent getViewer(Object value) {
            return new MethodInfoPanel((Value) value);
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
        };
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
                    return new TaxaComponent((Taxa) value);
                }
                return new TaxaComponent(((Value<Taxa>) value).value());
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
                stringValueViewer,
                booleanValueViewer,
                doubleArray2DViewer,
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

        private static Viewer getViewerForValue(Object object) {
            for (Viewer viewer : viewers) {
                if (viewer.match(object)) return viewer;
            }
            LoggerUtils.log.severe("Found no viewer for " + object);
            return null;
        }

        public static JComponent getJComponentForValue(Object object) {
            for (Viewer viewer : viewers) {
                if (viewer.match(object)) return viewer.getViewer(object);
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
