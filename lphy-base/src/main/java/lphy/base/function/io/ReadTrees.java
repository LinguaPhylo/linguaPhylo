package lphy.base.function.io;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.evolution.tree.WrappedJEBLTimeTreeNode;
import lphy.core.io.UserDir;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Newick or Nexus
 * @see lphy.base.function.tree.Newick
 */
public class ReadTrees extends DeterministicFunction<TimeTree[]> {

    public ReadTrees(@ParameterInfo(name = ReaderConst.FILE, narrativeName = "file name", description = "the name of Nexus/Newick file including path.") Value<String> filePath,
                     @ParameterInfo(name = ReaderConst.FORMAT, description = "Nexus or Newick (case-insesitive), default to Nexus.",
                             optional=true) Value<String> format ) {
        setParam(ReaderConst.FILE, filePath);
        // default to true
        if (format != null)
            setParam(ReaderConst.FORMAT, format);
        else setParam(ReaderConst.FORMAT, new Value<>(null, "nexus"));
    }

    @GeneratorInfo(name="readTrees", category = GeneratorCategory.TREE, examples = {"readTrees.lphy"},
            description = "A function that parses trees from either a Nexus (default) or Newick file.")
    public Value<TimeTree[]> apply() {
        String filePath = ((Value<String>) getParams().get(ReaderConst.FILE)).value();
        Path path = UserDir.getUserPath(filePath);

        String format = ((Value<String>) getParams().get(ReaderConst.FORMAT)).value();
        if (format == null ||
                !(format.equalsIgnoreCase("Nexus") || format.equalsIgnoreCase("Newick")) )
            throw new IllegalArgumentException("The argument value of " + ReaderConst.FORMAT +
                    " must be either Nexus or Newick ! But get " + format);

        TimeTree[] timeTrees = new TimeTree[0];

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            //*** parsing ***//
            TreeImporter treeImporter;
            // format = Newick
            if (format.equalsIgnoreCase("Newick")) {
                boolean unquotedLabels = false;
                treeImporter = new NewickImporter(reader, unquotedLabels);
            } else { //default
                boolean compactTrees = false;
                treeImporter = new NexusImporter(reader, compactTrees, 0);
            }

            List<jebl.evolution.trees.Tree> treeList = treeImporter.importTrees();

            timeTrees = new TimeTree[treeList.size()];
            for (int i = 0; i < treeList.size(); i++) {
                Tree jeblTree = treeList.get(i);
                timeTrees[i] = convert(jeblTree);
            }

        } catch (FileNotFoundException e) {
            LoggerUtils.log.severe("File " + Path.of(filePath).toAbsolutePath() + " is not found !\n" +
                    "The current working dir = " + UserDir.getUserDir());
        } catch (IOException | ImportException e) {
            LoggerUtils.logStackTrace(e);
        }

        return new Value<>(null, timeTrees, this);

    }

    private TimeTree convert(Tree jeblTree) {

        if (jeblTree instanceof SimpleRootedTree rootedTree) {

            Set<Taxon> jeblTaxa = rootedTree.getTaxa();
            Taxa taxa = Taxa.createTaxa(jeblTaxa.toArray());

            TimeTree timeTree = new TimeTree(taxa);

//TODO where is id in jebl nodes?

            TimeTreeNode root = new WrappedJEBLTimeTreeNode(rootedTree.getRootNode(), rootedTree, timeTree);
            timeTree.setRoot(root);

            return timeTree;

        } else throw new IllegalArgumentException("LPhy requires the rooted tree !");

    }

}
