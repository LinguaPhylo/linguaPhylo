package lphystudio.core.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.core.io.FileConfig;
import lphy.core.io.OutputSystem;
import lphy.core.logger.ValueFormatHandler;
import lphy.core.logger.ValueFormatResolver;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Value;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.simulator.SimulatorListener;
import lphy.core.spi.LoaderManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Write each alignment to a file during sampling.
 * The viewer is {@link lphystudio.app.graphicalmodelpanel.AlignmentLogPanel}.
 * This is a duplicated version of
 * but contains GUI code.
 * @author Walter Xie
 */
public class AlignmentLog extends JTextArea implements SimulatorListener {

    final LPhyMetaParser parser;

//    final Preferences preferences = Preferences.userNodeForPackage(AlignmentLog.class);
    static final String STUDIO_LOG_ALIGNMENT = "studio_log_alignment";

    boolean logAlignment = false;

    // index should match the replicate index
//    List<List<Value>> allAlgValues = new ArrayList<>();


    public AlignmentLog(LPhyMetaParser parser) {
        this.parser = parser;
    }

    public void clear() {
        setText("");
        setEditable(false);
    }

    public void setLogAlignment(boolean logAlignment) {
        this.logAlignment = logAlignment;
//        preferences.putBoolean(STUDIO_LOG_ALIGNMENT, bool);
    }

    public boolean toLogAlignment() {
        return logAlignment;
//        return preferences.getBoolean(STUDIO_LOG_ALIGNMENT, false);
    }

    public void setOutputDir(String dir) {
        OutputSystem.setOutputDirectory(dir);
    }

    public String getOutputDir() {
        return OutputSystem.getOutputDirectory().getAbsolutePath();
    }


    @Override
    public void start(List<Object> configs) {
//        allAlgValues.clear();
    }

    @Override
    public void replicate(int index, List<Value> values) {
        // can be SimpleAlignment or SimpleAlignment[]
        // exclude clamped alignment
        List<Value> alignmentValuePerRep = getSimulatedAlignmentValues(values);

        if (index == 0) {
//            allAlgValues.clear();
            setText("sample");
            for (int i = 0; i < alignmentValuePerRep.size(); i++) {
                Value alV = alignmentValuePerRep.get(i);

                List<ValueFormatter> valueFormatterList = ValueFormatResolver
                        .createFormatter(AlignmentTextFormatter.class, alV);
                for (ValueFormatter valueFormatter : valueFormatterList) {
                    append("\t" + valueFormatter.header());
                }
            }
            append("\n");
        }

        append(index+"");
        for (int i = 0; i < alignmentValuePerRep.size(); i++) {
            Value<SimpleAlignment> alV = alignmentValuePerRep.get(i);

            List<ValueFormatter> valueFormatterList = ValueFormatResolver
                    .createFormatter(AlignmentTextFormatter.class, alV);
            for (ValueFormatter valueFormatter : valueFormatterList) {
                append("\t" + valueFormatter.format(alV.value()));

                if (toLogAlignment()) {
                    logAlignment(index, alV);
                }
            }
        }
        append("\n");

        // store all alignments
//        allAlgValues.add(alignmentValuePerRep);
    }

    @Override
    public void complete() {

    }

    // can be SimpleAlignment or SimpleAlignment[]
    private List<Value> getSimulatedAlignmentValues(List<Value> variables) {
        List<Value> values = new ArrayList<>();
        for (Value<?> v : variables) {
            if (v.value() instanceof SimpleAlignment || v.value() instanceof SimpleAlignment[]) {
                // exclude clamped alignment
                if (! parser.isClampedVariable(v))
                    values.add(v);
            }
        }
        return values;
    }

    private void logAlignment(int index, Value<SimpleAlignment> alignmentValue) {

        ValueFormatter formatter = LoaderManager.valueFormatResolver.getDefaultFormatter(alignmentValue);

        if (formatter != null) {

            String filePrefix = parser.getName();
            String fileExtension = formatter.getExtension();
            // If value is array, the id will be appended with index
            String id = formatter.getValueID();

            //TODO how to pass numReplicates from TextField to here
            // e.g. 1 alignment per file
            // if maxId > 0, add postfix, e.g. _0.nexus
            String fileName = FileConfig
                    .getOutFileName(id, index, 1000, filePrefix, fileExtension);

            ValueFormatHandler.createFile(fileName);

            ValueFormatHandler.ValuePerFile
                    .exportValuePerFile(index, alignmentValue, formatter);

        } else
            JOptionPane.showMessageDialog(this,
                    "Cannot find the default formatter to write alignment " + alignmentValue.getId() + " !");

    }
//    private void logAlignment(Value<SimpleAlignment> alignment, int rep) {
//        Path dir = UserDir.getAlignmentDir();
//        String fileName = alignment.getCanonicalId() + "_" + rep + ".nexus";
//        PrintStream stream = null;
//        try {
//            File file = Paths.get(dir.toString(), fileName).toFile();
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//                // throw new IllegalArgumentException("Directory " + file.getParentFile() + " does not exist !");
//            }
//            stream = new PrintStream(file);
//            // no tree
//            NexusWriter.write(alignment.value(), new LinkedList<>(), stream);
//
//            LoggerUtils.log.info("Sample " + rep + " writes alignment " + alignment.getCanonicalId() + " to " + file);
//        } catch (Exception e) {
//            LoggerUtils.logStackTrace(e);
//            e.printStackTrace();
//        } finally {
//            NexusWriter.close(stream);
//        }
//    }

}
