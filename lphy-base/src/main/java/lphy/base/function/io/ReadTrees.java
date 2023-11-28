//package lphy.base.function.io;
//
//import jebl.evolution.io.ImportException;
//import jebl.evolution.io.NewickImporter;
//import jebl.evolution.io.NexusImporter;
//import jebl.evolution.io.TreeImporter;
//import jebl.evolution.trees.Tree;
//import lphy.base.evolution.alignment.MetaDataAlignment;
//import lphy.base.evolution.tree.TimeTree;
//import lphy.base.function.alignment.MetaDataOptions;
//import lphy.base.parser.NexusParser;
//import lphy.core.io.UserDir;
//import lphy.core.logger.LoggerUtils;
//import lphy.core.model.DeterministicFunction;
//import lphy.core.model.Value;
//import lphy.core.model.annotation.GeneratorCategory;
//import lphy.core.model.annotation.GeneratorInfo;
//import lphy.core.model.annotation.ParameterInfo;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//
///** TODO It requires jebl3 to export jebl.evolution.trees.Tree
// * Newick or Nexus
// * @see lphy.base.function.tree.Newick
// */
//public class ReadTrees extends DeterministicFunction<TimeTree[]> {
//
//    public ReadTrees(@ParameterInfo(name = ReaderConst.FILE, narrativeName = "file name", description = "the name of Nexus/Newick file including path.") Value<String> filePath,
//                     @ParameterInfo(name = ReaderConst.FORMAT, description = "Nexus or Newick (case-insesitive), default to Nexus.",
//                             optional=true) Value<String> format ) {
//        setParam(ReaderConst.FILE, filePath);
//        // default to true
//        if (format != null)
//            setParam(ReaderConst.FORMAT, format);
//        else setParam(ReaderConst.FORMAT, new Value<>(null, "nexus"));
//    }
//
//    @GeneratorInfo(name="readTrees", category = GeneratorCategory.TREE, examples = {"readTrees.lphy"},
//            description = "A function that parses trees from either a Nexus (default) or Newick file.")
//    public Value<TimeTree[]> apply() {
//        String filePath = ((Value<String>) getParams().get(ReaderConst.FILE)).value();
//        Path path = UserDir.getUserPath(filePath);
//
//        String format = ((Value<String>) getParams().get(ReaderConst.FORMAT)).value();
//        if (format == null ||
//                !(format.equalsIgnoreCase("Nexus") || format.equalsIgnoreCase("Newick")) )
//            throw new IllegalArgumentException("The argument value of " + ReaderConst.FORMAT +
//                    " must be either Nexus or Newick ! But get " + format);
//
//        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
//            //*** parsing ***//
//            TreeImporter treeImporter;
//            if (format.equalsIgnoreCase("Newick")) {
//                boolean unquotedLabels = false;
//                treeImporter = new NewickImporter(reader, unquotedLabels);
//            } else {
//                boolean compactTrees = false;
//                treeImporter = new NexusImporter(reader, compactTrees, 0);
//            }
//
//            treeImporter.importTrees();
//
//            List<jebl.evolution.trees.Tree>
//
//
//        } catch (FileNotFoundException e) {
//            LoggerUtils.log.severe("File " + Path.of(filePath).toAbsolutePath() + " is not found !\n" +
//                    "The current working dir = " + UserDir.getUserDir());
//        } catch (IOException e) {
//            LoggerUtils.logStackTrace(e);
//        }
//
//
//
//        //*** parsing ***//
//        NexusImporter nexusImporter = new NexusImporter(path.toString());
//
//        MetaDataAlignment nexusData = null;
//        try {
//            // if ageDirectionStr = null, then assume forward
//            nexusData = nexusParser.importNexus(ageDirectionStr);
//        } catch (IOException | ImportException e) {
//            LoggerUtils.logStackTrace(e);
//        }
//        // set age to Taxon
//        if (ageRegxStr != null) {
//            if (! Objects.requireNonNull(nexusData).isUltrametric())
//                LoggerUtils.log.severe("Taxa ages had been imported from the nexus file ! " +
//                        "It would be problematic to overwrite taxa ages from the command line !");
//
//            nexusData.setAgesParsedFromTaxaName(ageRegxStr, ageDirectionStr);
//        }
//
//        // set species to Taxon
//        if (spRegxStr != null)
//            Objects.requireNonNull(nexusData).setSpeciesParsedFromTaxaName(spRegxStr);
//
//        return new Value<>(null, nexusData, this);
//
//    }
//
//}
