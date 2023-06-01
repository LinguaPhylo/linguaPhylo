package lphystudio.app.docgenerator;

import lphy.core.model.components.*;
import lphy.core.parser.ParserLoader;
import lphy.core.spi.LPhyLoader;
import lphy.core.util.LoggerUtils;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static lphy.core.model.components.GeneratorCategory.*;

/**
 * Delete everything under ???/lphy/doc, if it is not a minor change to docs.
 * For LPhy core, set working directory: ~/WorkSpace/linguaPhylo/docs,
 * and args[0] = version.
 * For extension, set working directory: ~/WorkSpace/$REPO/docs,
 * and args[0] = version, args[1] = extension name (no space),
 * args[2] = class name to implement LPhyExtension.
 *
 * Use "user.dir" to change the output directory
 */
public class GenerateDocs {

    public static final String PARAM_DIR = "parametric";
    public static final String TREE_MODEL_DIR = "tree-model";
    public static final String OTHER_DIST_DIR = "distributions";
    public static final String SEQU_TYPE_DIR = "sequence-type";
    public static final String TAXA_ALIG_DIR = "taxa-alignment";
    public static final String SUBST_SITE_MODEL_DIR = "subst-site-model";
    public static final String TREE_FUNC_DIR = "tree-func";
    public static final String OTHER_FUNC_DIR = "functions";
    public static final String TYPES_DIR = "types";
    // No white space
    static final String LPHY_DOC_TITLE = "LPhy";

    public static void main(String[] args) throws IOException {

        String version = "";
        if (args.length > 0)  version = args[0];

        LPhyLoader lphyLoader = LPhyLoader.getInstance();

        // Do not change default
        String extName = LPHY_DOC_TITLE;
        // for extension only, e.g.
        // args = 0.0.5 "LPhy Extension Phylonco" phylonco.lphy.spi.Phylonco
        // set WD = ~/WorkSpace/beast-phylonco/PhyloncoL/doc
        if (args.length > 2)  {
            extName = args[1];
            // class name with package that implements {@link LPhyExtension}
            String clsName = args[2];
            lphyLoader.loadExtension(clsName);
        }
        System.out.println("Creating doc for " + extName + " ...\n");

        List<Class<GenerativeDistribution>> generativeDistributions = ParserLoader.getGenerativeDistributions();
        generativeDistributions.sort(Comparator.comparing(Class::getSimpleName));

        List<Class<DeterministicFunction>> functions = ParserLoader.getDeterministicFunctions();
        functions.sort(Comparator.comparing(Class::getSimpleName));

        // output dir
        final Path dir = Paths.get(System.getProperty("user.dir"));
        // out dir must be $project$/lphy/doc
        if (isLPhyDoc(extName)) {
            if (! (dir.endsWith("lphy" + File.separator + "doc") ||
                    dir.endsWith("docs"))  )
                throw new IllegalArgumentException("The user.dir must set to either $project$/docs or $project$/lphy/doc !\n" + dir.toAbsolutePath());
        }
        System.out.println("Creating " + extName + " docs to " + dir.toAbsolutePath() + "\n");

        String indexMD = generateIndex(generativeDistributions, functions, ParserLoader.types, dir, version, extName);

        File f = new File(dir.toString(), "index.md");
        FileWriter writer = new FileWriter(f);
        writer.write(indexMD);
        writer.close();

    }

    private static boolean isLPhyDoc(String extName) {
        return LPHY_DOC_TITLE.equalsIgnoreCase(extName.trim());
    }
    
//    private Properties properties;
//
//    public void getMavenProperties() throws IOException {
//        InputStream is = getClass().getClassLoader().getResourceAsStream("pom.xml");
//        this.properties = new Properties();
//        this.properties.load(is);
//    }
//
//    public String getProperty(String propertyName) {
//        return Objects.requireNonNull(this.properties).getProperty(propertyName);
//    }

    private static String getDistDir(Class<GenerativeDistribution> genDist) {
        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(genDist);
        GeneratorCategory category = null;
        if (generatorInfo != null) {
            category = generatorInfo.category();
        } else {
            LoggerUtils.log.severe("GeneratorInfo annotation is not found from class "+ genDist + " !");
            category = GeneratorCategory.NONE;
        }

        return switch (category) {
            case PRIOR                -> PARAM_DIR;
            case COAL_TREE, BD_TREE   -> TREE_MODEL_DIR;
            default                   -> OTHER_DIST_DIR;
        };
    }

    private static String getFuncDir(Class<DeterministicFunction> func) {
        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(func);
        GeneratorCategory category = null;
        if (generatorInfo != null) {
            category = generatorInfo.category();
        } else {
            LoggerUtils.log.severe("GeneratorInfo annotation is not found from class "+ func + " !");
            category = NONE;
        }

        return switch (category) {
            case SEQU_TYPE      -> SEQU_TYPE_DIR;
            case TAXA_ALIGNMENT -> TAXA_ALIG_DIR;
            case RATE_MATRIX, SITE_MODEL, MODEL_AVE_SEL  -> SUBST_SITE_MODEL_DIR;
            case TREE           -> TREE_FUNC_DIR;
            default             -> OTHER_FUNC_DIR;
        };
    }

    private static String addHomepageURL(String text) {
        return "<a href=\"https://linguaphylo.github.io\">" + text + "</a>";
    }

    // output to dir
    private static String generateIndex(List<Class<GenerativeDistribution>> generativeDistributions,
                                        List<Class<DeterministicFunction>> functions, Set<Class<?>> types,
                                        Path dir, String version, String extName) throws IOException {
        File otherDistDir = new File(dir.toString(),OTHER_DIST_DIR);
        File paramDir = new File(dir.toString(),PARAM_DIR);
        File treeModelDir = new File(dir.toString(),TREE_MODEL_DIR);
        File sequTypeDir = new File(dir.toString(),SEQU_TYPE_DIR);
        File taxaAligDir = new File(dir.toString(),TAXA_ALIG_DIR);
        File substSiteDir = new File(dir.toString(),SUBST_SITE_MODEL_DIR);
        File treeFuncDir = new File(dir.toString(),TREE_FUNC_DIR);
        File otherFuncDir = new File(dir.toString(),OTHER_FUNC_DIR);
        File typesDir = new File(dir.toString(), "types");

        /**
         * Title
         */
        StringBuilder builder = new StringBuilder();
        // add url link
        if (LPHY_DOC_TITLE.equalsIgnoreCase(extName))
            extName = addHomepageURL(extName);
        String h1 = extName + " Language Reference";
        if (version != null || !version.trim().isEmpty())
            h1 += " (version " + version + ")";
        builder.append(new Heading(h1, 1)).append("\n");

        builder.append(new Text("This an automatically generated language reference " +
                "of the " + addHomepageURL("LinguaPhylo") +
                " (LPhy) statistical phylogenetic modeling language."));
        builder.append("\n\n");

        /**
         * classify GenerativeDistribution
         */
        List<Link> paramDistLinks = new ArrayList<>();
        List<Link> treeModelLinks = new ArrayList<>();
        List<Link> otherDistLinks = new ArrayList<>();

        Set<String> names = new TreeSet<>();

        for (Class<GenerativeDistribution> genDist : generativeDistributions) {
            String name = Generator.getGeneratorName(genDist);
            String subDir = getDistDir(genDist);
            // based on where index.md is
            String fileURL = subDir + "/" + name + ".md";

            if (!names.contains(name)) {
                Link link = new Link(name, fileURL);
                switch (subDir) {
                    case PARAM_DIR -> {
                        paramDistLinks.add(link);
                        if (!paramDir.exists()) paramDir.mkdir(); }
                    case TREE_MODEL_DIR -> {
                        treeModelLinks.add(link);
                        if (!treeModelDir.exists()) treeModelDir.mkdir(); }
                    default -> {
                        otherDistLinks.add(link);
                        if (!otherDistDir.exists()) otherDistDir.mkdir();
                    }
                }
                names.add(name);
                FileWriter writer = new FileWriter(new File(dir.toString(), fileURL));

                generateGenerativeDistributions(writer, name, generativeDistributions.stream().filter(o -> Generator.getGeneratorName(o).equals(name)).collect(Collectors.toList()));
                writer.close();
            }
        }

        if (paramDistLinks.size() > 0) {
            builder.append(new Heading(PRIOR.getName(), 2)).append("\n");
            builder.append(new UnorderedList<>(paramDistLinks)).append("\n\n");
        }
        if (treeModelLinks.size() > 0) {
            builder.append(new Heading("Tree models", 2)).append("\n");
            builder.append(new UnorderedList<>(treeModelLinks)).append("\n\n");
        }
        if (otherDistLinks.size() > 0) {
            builder.append(new Heading("Other generative distributions", 2)).append("\n");
            builder.append(new UnorderedList<>(otherDistLinks)).append("\n\n");
        }


        /**
         * classify DeterministicFunction
         */
        List<Link> seqTypeLinks = new ArrayList<>();
        List<Link> taxaAligLinks = new ArrayList<>();
        List<Link> substSiteLinks = new ArrayList<>();
        List<Link> treeFuncLinks = new ArrayList<>();
        List<Link> otherFuncLinks = new ArrayList<>();

        Set<String> funcNames = new TreeSet<>();

        for (Class<DeterministicFunction> function : functions) {
            String name = Generator.getGeneratorName(function);
            String subDir = getFuncDir(function);
            // based on where index.md is
            String fileURL = subDir + "/" + name + ".md";

            if (!funcNames.contains(name)) {
                Link link = new Link(name, fileURL);
                switch (subDir) {
                    case SUBST_SITE_MODEL_DIR -> {
                        substSiteLinks.add(link);
                        if (!substSiteDir.exists()) substSiteDir.mkdir(); }
                    case SEQU_TYPE_DIR -> {
                        seqTypeLinks.add(link);
                        if (!sequTypeDir.exists()) sequTypeDir.mkdir(); }
                    case TAXA_ALIG_DIR -> {
                        taxaAligLinks.add(link);
                        if (!taxaAligDir.exists()) taxaAligDir.mkdir(); }
                    case TREE_FUNC_DIR -> {
                        treeFuncLinks.add(link);
                        if (!treeFuncDir.exists()) treeFuncDir.mkdir(); }
                    default -> {
                        otherFuncLinks.add(link);
                        if (!otherFuncDir.exists()) otherFuncDir.mkdir();
                    }
                }
                funcNames.add(name);

                FileWriter writer = new FileWriter(new File(dir.toString(), fileURL));

                generateFunctions(writer, name, functions.stream().filter(o -> Generator.getGeneratorName(o).equals(name)).collect(Collectors.toList()));
                writer.close();
            }
        }

        if (seqTypeLinks.size() > 0) {
            builder.append(new Heading(SEQU_TYPE.getName(), 2)).append("\n");
            builder.append(new UnorderedList<>(seqTypeLinks)).append("\n\n");
        }
        if (taxaAligLinks.size() > 0) {
            builder.append(new Heading("Taxa & alignment", 2)).append("\n");
            builder.append(new UnorderedList<>(taxaAligLinks)).append("\n\n");
        }
        if (substSiteLinks.size() > 0) {
            builder.append(new Heading("Substitution and site models", 2)).append("\n");
            builder.append(new UnorderedList<>(substSiteLinks)).append("\n\n");
        }
        if (treeFuncLinks.size() > 0) {
            builder.append(new Heading(TREE.getName(), 2)).append("\n");
            builder.append(new UnorderedList<>(treeFuncLinks)).append("\n\n");
        }
        if (otherFuncLinks.size() > 0) {
            builder.append(new Heading("Other functions", 2)).append("\n");
            builder.append(new UnorderedList<>(otherFuncLinks)).append("\n\n");
        }

        /**
         * Types
         */
        List<Link> typeLinks = new ArrayList<>();
        Set<String> typeNames = new TreeSet<>();

        for (Class<?> type : types) {
            String name = type.getSimpleName();
            // based on where index.md is
            String fileURL = TYPES_DIR + "/" + name + ".md";

            if (!typeNames.contains(name)) {
                typeLinks.add(new Link(name, fileURL));
                typeNames.add(name);
                if (!typesDir.exists()) typesDir.mkdir();

                FileWriter writer = new FileWriter(new File(dir.toString(), fileURL));

                if (GeneratorMarkdown.SEQU_TYPE.equals(name))
                    writer.write(GeneratorMarkdown.generateSequenceTypeMarkdown());
                else
                    writer.write(GeneratorMarkdown.generateTypeMarkdown(type));
                writer.close();
            }
        }
        if (typeLinks.size() > 0) {
            builder.append(new Heading("Types", 2)).append("\n");
            builder.append(new UnorderedList<>(typeLinks)).append("\n\n");
        }

        /**
         * Built-in, only generated for LPhy core doc
         */
        if (isLPhyDoc(extName)) {
            List<Link> builtin = List.of(new Link("binary operators functions","built-in-binary-operators.md"),
                    new Link("math functions","built-in-math.md"),
                    new Link("trigonometric functions","built-in-trigonometry.md") );
            builder.append(new Heading("Built-in", 2)).append("\n");
            builder.append(new UnorderedList<>(builtin)).append("\n\n");
        }

        return builder.toString();
    }

    private static void generateGenerativeDistributions(FileWriter writer, String name, List<Class<?>> classes) throws IOException {

        StringBuilder builder = new StringBuilder();

        builder.append(new Heading(name + " distribution",1)).append("\n");

        for (Class<?> c : classes) {

//            if (LGenerator.class.isAssignableFrom(c)) {
                //TODO not sure if LGenerativeDistribution is still required
//                builder.append(LGenerativeDistribution.getLightweightGeneratorMarkdown((Class<LGenerator>)c)).append("\n\n");
//            } else {
                builder.append(GeneratorMarkdown.getGeneratorMarkdown((Class<Generator>)c, TYPES_DIR)).append("\n\n");
//            }
        }

        writer.write(builder.toString());
    }

    private static void generateFunctions(FileWriter writer, String name, List<Class<DeterministicFunction>> classes) throws IOException {

        StringBuilder builder = new StringBuilder();

        builder.append(new Heading(name + " function",1)).append("\n");

        for (Class<DeterministicFunction> c : classes) {
            builder.append(GeneratorMarkdown.getGeneratorMarkdown(c, TYPES_DIR)).append("\n\n");
        }

        writer.write(builder.toString());
    }

}
