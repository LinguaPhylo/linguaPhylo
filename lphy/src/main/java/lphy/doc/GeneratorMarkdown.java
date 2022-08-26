package lphy.doc;

import lphy.graphicalModel.Citation;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.util.ArrayList;
import java.util.List;

/**
 * An util class to create lphy doc in markdown.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class GeneratorMarkdown {

    static String getTypeURL(final String typesDir, final String name) {
       return  "../" + typesDir + "/" + name + ".md";
    }

    static String getGeneratorMarkdown(Class<? extends Generator> generatorClass, final String typesDir) {

        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(generatorClass);

        List<ParameterInfo> pInfo = Generator.getParameterInfo(generatorClass, 0);
        Class[] types = Generator.getParameterTypes(generatorClass, 0);

        StringBuilder md = new StringBuilder();

        StringBuilder signature = new StringBuilder();

        signature.append(Generator.getGeneratorName(generatorClass)).append("(");

        int count = 0;
        for (int i = 0; i < pInfo.size(); i++) {
            ParameterInfo pi = pInfo.get(i);
            if (count > 0) signature.append(", ");
            String typeName = types[i].getSimpleName();
            Link typeLink = new Link(typeName, getTypeURL(typesDir, typeName));
            signature.append(typeLink).append(" ").append(new BoldText(pi.name()));
            count += 1;
        }
        signature.append(")");

        md.append(new Heading(signature.toString(), 2)).append("\n\n");

        if (generatorInfo != null) md.append(generatorInfo.description()).append("\n\n");

        if (pInfo.size() > 0) {
            md.append(new Heading("Parameters", 3)).append("\n\n");
            List<Object> paramText = new ArrayList<>();

            for (int i = 0; i < pInfo.size(); i++) {
                ParameterInfo pi = pInfo.get(i);
                String typeName = types[i].getSimpleName();
                Link typeLink = new Link(typeName,getTypeURL(typesDir, typeName));
                paramText.add(new Text(typeLink + " " + new BoldText(pi.name()) + " - " + pi.description()));
            }
            md.append(new UnorderedList<>(paramText));
        }
        md.append("\n\n");

        md.append(new Heading("Return type", 3)).append("\n\n");

        String returnTypeName = Generator.getReturnType(generatorClass).getSimpleName();
        Link returnTypeLink = new Link(returnTypeName,getTypeURL(typesDir, returnTypeName));
        md.append(returnTypeLink).append("\n\n");

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
