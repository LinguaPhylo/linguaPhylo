package lphy.core.lightweight;

import lphy.core.graphicalmodel.components.Argument;
import lphy.core.graphicalmodel.components.Citation;
import lphy.core.graphicalmodel.components.Generator;
import lphy.core.graphicalmodel.components.GeneratorInfo;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface LGenerativeDistribution<T> extends LGenerator<T> {

    @Override
    default boolean isRandomGenerator() {
        return true;
    }

    default T sample() { return generateRaw(); }

    default double density(T value) {
        return Math.exp(logDensity(value));
    }

    default double logDensity(T value) {
        return Math.log(density(value));
    }

    default T generateRaw() { return sample(); }

    static String getLightweightGeneratorMarkdown(Class<? extends LGenerator> generatorClass) {

        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(generatorClass);

        List<Argument> arguments = LGenerator.getArguments(generatorClass,0);

        StringBuilder md = new StringBuilder();

        StringBuilder signature = new StringBuilder();

        signature.append(Generator.getGeneratorName(generatorClass)).append("(");

        int count = 0;
        for (Argument argument : arguments) {
            if (count > 0) signature.append(", ");
            signature.append(new Text(argument.type.getSimpleName())).append(" ").append(new BoldText(argument.name));
            count += 1;
        }
        signature.append(")");

        md.append(new Heading(signature.toString(), 2)).append("\n\n");

        if (generatorInfo != null) md.append(generatorInfo.description()).append("\n\n");

        if (arguments.size() > 0) {
            md.append(new Heading("Parameters", 3)).append("\n\n");
            List<Object> paramText = new ArrayList<>();

            for (Argument argument : arguments) {
                paramText.add(new Text(argument.type.getSimpleName() + " " + new BoldText(argument.name) + " - " + argument.description));
            }
            md.append(new UnorderedList<>(paramText));
        }
        md.append("\n\n");

        try {
            md.append(new Heading("Return type", 3)).append("\n\n");
            Class returnTypeClass = generatorClass.getMethod("sample").getReturnType();
            List<String> returnType = Collections.singletonList(returnTypeClass.getSimpleName());
            md.append(new UnorderedList<>(returnType)).append("\n\n");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Citation citation = Generator.getCitation(generatorClass);
        if (citation != null) {
            md.append(new Heading("Reference", 3)).append("\n\n");
            md.append(citation.value());
            if (citation.DOI().length() > 0) {
                String url = citation.DOI();
                if (!url.startsWith("http")) {
                    url = "http://doi.org/" + url;
                }
                md.append(new Link(url, url));
            }
        }
        return md.toString();
    }
}

