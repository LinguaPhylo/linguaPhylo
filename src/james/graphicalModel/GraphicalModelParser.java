package james.graphicalModel;

import james.Coalescent;
import james.core.JCPhyloCTMC;
import james.core.PhyloBrownian;
import james.core.PhyloCTMC;
import james.core.distributions.Dirichlet;
import james.core.distributions.Exp;
import james.core.distributions.LogNormal;
import james.core.distributions.Normal;
import james.graphicalModel.types.DoubleArrayValue;
import james.graphicalModel.types.DoubleValue;
import james.graphicalModel.types.IntegerArrayValue;
import james.graphicalModel.types.IntegerValue;
import james.app.GraphicalModelChangeListener;
import james.app.GraphicalModelListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class GraphicalModelParser {

    private SortedMap<String, Value> dictionary = new TreeMap<>();

    Set<String> globalArguments = new TreeSet<>();

    Map<String, Class> genDistDictionary = new TreeMap<>();

    Map<String, Class> functionDictionary = new TreeMap<>();

    List<String> lines = new ArrayList<>();

    List<GraphicalModelChangeListener> listeners = new ArrayList<>();
    List<GraphicalModelListener> gmListeners = new ArrayList<>();

    enum Keyword {
        remove
    }

    public GraphicalModelParser() {

        Class[] genClasses = {Normal.class, LogNormal.class, Exp.class, Coalescent.class, JCPhyloCTMC.class, PhyloCTMC.class, PhyloBrownian.class, Dirichlet.class};

        for (Class genClass : genClasses) {
            genDistDictionary.put(genClass.getSimpleName(), genClass);
        }

        functionDictionary.put("exp", james.core.functions.Exp.class);
        functionDictionary.put("jukesCantor", james.core.functions.JukesCantor.class);
        functionDictionary.put("k80", james.core.functions.K80.class);
        functionDictionary.put("hky", james.core.functions.HKY.class);
        functionDictionary.put("gtr", james.core.functions.GTR.class);
    }

    public void addGraphicalModelChangeListener(GraphicalModelChangeListener listener) {
        listeners.add(listener);
    }

    public void addGraphicalModelListener(GraphicalModelListener listener) {
        gmListeners.add(listener);
    }

    public SortedMap<String, Value> getDictionary() {
        return dictionary;
    }

    public Set<Value> getRoots() {
        Set<String> nonArguments = new HashSet<>();
        dictionary.values().forEach((val) -> nonArguments.add(val.getId()));
        nonArguments.removeAll(globalArguments);

        SortedSet<Value> nonArgValues = new TreeSet<>(Comparator.comparing(Value::getId));
        nonArguments.forEach((id) -> nonArgValues.add(dictionary.get(id)));

        return nonArgValues;
    }

    public void parseLines(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            parseLine(lines[i]);
        }
    }

    public void parseLine(String line) {
        int lineNumber = nextLineNumber();
        if (isRandomVariableLine(line)) {
            parseRandomVariable(line, lineNumber);
        } else if (isFunctionLine(line)) {
            parseFunctionLine(line, lineNumber);
        } else if (isFixedParameterLine(line)) {
            parseFixedParameterLine(line, lineNumber);
        } else if (isKeywordLine(line)) {
            parseKeywordLine(line, lineNumber);
        } else if (isValueId(line)) {
            selectValue(dictionary.get(line.substring(0, line.length() - 1)));
        } else {
            throw new RuntimeException("Parse error on line " + lineNumber + ": " + line);
        }
        lines.add(line);
        notifyListeners();
    }

    private void selectValue(Value value) {
        for (GraphicalModelListener listener : gmListeners) {
            listener.valueSelected(value);
        }
    }

    private boolean isValueId(String line) {
        return dictionary.keySet().contains(line.substring(0, line.length() - 1));
    }

    private void parseKeywordLine(String line, int lineNumber) {
        line = line.trim();
        Keyword keyword = null;
        for (Keyword kw : Keyword.values()) {
            if (line.startsWith(kw.name())) {
                keyword = kw;
                break;
            }
        }
        String remainder = line.substring(keyword.name().length());
        switch (keyword) {
            case remove:
                parseRemove(remainder, lineNumber);
        }
    }

    private void parseRemove(String remainder, int lineNumber) {
        if (remainder.startsWith("(")) {
            remainder = remainder.trim();
            if (remainder.endsWith(");")) {
                String argument = remainder.substring(1, remainder.length() - 2);
                if (dictionary.keySet().contains(argument)) {
                    dictionary.remove(argument);
                    System.out.println("Removed " + argument + ".");
                } else {
                    System.out.println("Value named " + argument + " not found.");
                }
            } else
                throw new RuntimeException("Parsing error: expected ')' after argument to keyword " + Keyword.remove);

        } else throw new RuntimeException("Parsing error: expected '(' after keyword " + Keyword.remove);
    }

    private boolean isKeywordLine(String line) {
        line = line.trim();
        for (Keyword keyword : Keyword.values()) {
            if (line.startsWith(keyword.name())) {
                return true;
            }
        }
        return false;
    }

    private void notifyListeners() {
        for (GraphicalModelChangeListener listener : listeners) {
            listener.modelChanged();
        }
    }

    private int nextLineNumber() {
        return lines.size();
    }

    private void parseFixedParameterLine(String line, int lineNumber) {
        String[] parts = line.split("=");
        if (parts.length != 2)
            throw new RuntimeException("Parsing fixed parameter " + parts[0] + " failed on line " + lineNumber);
        String id = parts[0].trim();
        String valueString = parts[1].substring(0, parts[1].indexOf(';')).trim();
        Value literalValue = parseLiteralValue(id, valueString, lineNumber);
        dictionary.put(literalValue.getId(), literalValue);
    }

    private Value parseLiteralValue(String id, String valueString, int lineNumber) {

        if (valueString.startsWith("[") && valueString.endsWith("]")) {
            return parseList(id, valueString, lineNumber);
        }
        try {
            Integer intVal = Integer.parseInt(valueString);
            return new IntegerValue(id, intVal);
        } catch (NumberFormatException e) {
        }

        try {
            Double val = Double.parseDouble(valueString);
            return new DoubleValue(id, val);
        } catch (NumberFormatException e) {
        }

        throw new RuntimeException("Parsing fixed parameter " + id + " with value " + valueString + " failed on line " + lineNumber);
    }

    private Value parseList(String id, String valueString, int lineNumber) {
        String[] elements = valueString.substring(1, valueString.length() - 1).split(",");
        for (int i = 0; i < elements.length; i++) {
            elements[i] = elements[i].trim();
        }

        if (isInteger(elements[0])) {
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < elements.length; i++) {
                try {
                    values.add(Integer.parseInt(elements[i]));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parser error: parsing integer list at line number " + lineNumber + " but found non-integer:" + elements[i]);
                }
                return new IntegerArrayValue(id, values.toArray(new Integer[values.size()]));
            }
        } else if (isDouble(elements[0])) {
            List<Double> values = new ArrayList<>();
            for (int i = 0; i < elements.length; i++) {
                try {
                    values.add(Double.parseDouble(elements[i]));

                } catch (NumberFormatException e) {
                    throw new RuntimeException("Parser error: parsing real list at line number " + lineNumber + " but found non-real:" + elements[i]);
                }
            }
            return new DoubleArrayValue(id, values.toArray(new Double[values.size()]));
        }
        throw new RuntimeException("Parser error: parsing number list at line number " + lineNumber + " but found non-number:" + elements[0]);
    }

    private boolean isInteger(String s) {
        try {
            Integer intVal = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String s) {
        try {
            Double doubleVal = Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void parseFunctionLine(String line, int lineNumber) {
        int firstEquals = line.indexOf('=');
        String id = line.substring(0, firstEquals).trim();
        String remainder = line.substring(firstEquals + 1);
        String functionString = remainder.substring(0, remainder.indexOf(';'));
        Value val = parseDeterministicFunction(id, functionString, lineNumber);
        dictionary.put(val.getId(), val);
    }

//    private Value parseFunction(String id, String functionString, int lineNumber) {
//        String[] parts = functionString.split("\\(");
//        if (parts.length != 2)
//            throw new RuntimeException("Parsing function " + parts[0] + "failed on line " + lineNumber);
//        String funcName = parts[0].trim();
//        String argumentString = parts[1].substring(0, parts[1].indexOf(')')).trim();
//        Value argument = dictionary.get(argumentString);
//        if (argument == null) {
//            System.err.println(dictionary);
//            throw new RuntimeException("Couldn't find argument " + argumentString + " when parsing " + funcName + " on line " + lineNumber);
//        } else {
//            globalArguments.add(argument);
//        }
//
//        Class functionClass = functionDictionary.get(funcName);
//        try {
//            Function function = (Function) functionClass.newInstance();
//            Value result = function.apply(argument, id);
//            return result;
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Parsing function " + funcName + " failed on line " + lineNumber);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Parsing function " + funcName + " failed on line " + lineNumber);
//        }
//    }

    private Value parseDeterministicFunction(String id, String functionString, int lineNumber) {
        String[] parts = functionString.split("\\(");
        if (parts.length != 2)
            throw new RuntimeException("Parsing deterministic function " + parts[0] + "failed on line " + lineNumber);
        String name = parts[0].trim();
        String argumentString = parts[1].substring(0, parts[1].indexOf(')'));
        Map<String, String> arguments = parseArguments(argumentString, lineNumber);

        Class functionClass = functionDictionary.get(name);
        if (functionClass == null)
            throw new RuntimeException("Parsing error: Unrecognised deterministic function: " + name);

        try {
            Object[] initargs = new Object[arguments.keySet().size()];
            Constructor constructor = getConstructorByArguments(arguments, functionClass, initargs);
            if (constructor == null) {
                System.err.println("Function class: " + functionClass);
                System.err.println("     Arguments: " + arguments);
                throw new RuntimeException("Parser error: no constructor found for deterministic function " + name + " with arguments " + arguments);
            }

            DeterministicFunction func = (DeterministicFunction) constructor.newInstance(initargs);
            for (String parameterName : arguments.keySet()) {
                Value value = dictionary.get(arguments.get(parameterName));
                func.setParam(parameterName, value);
                globalArguments.add(value.id);
            }
            Value val = func.apply();
            val.setId(id);
            return val;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        }
    }

    private void parseRandomVariable(String line, int lineNumber) {
        String[] parts = line.split("~");
        if (parts.length != 2)
            throw new RuntimeException("Parsing random variable " + parts[0] + "failed on line " + lineNumber);
        String id = parts[0].trim();
        String genString = parts[1].substring(0, parts[1].indexOf(';'));
        GenerativeDistribution genDist = parseGenDist(genString, lineNumber);
        RandomVariable var = genDist.sample(id);
        dictionary.put(var.getId(), var);
    }

    private GenerativeDistribution parseGenDist(String genString, int lineNumber) {
        String[] parts = genString.split("\\(");
        if (parts.length != 2)
            throw new RuntimeException("Parsing generative distribution " + parts[0] + "failed on line " + lineNumber);
        String name = parts[0].trim();
        String argumentString = parts[1].substring(0, parts[1].indexOf(')'));
        Map<String, String> arguments = parseArguments(argumentString, lineNumber);

        Class genDistClass = genDistDictionary.get(name);
        if (genDistClass == null)
            throw new RuntimeException("Parsing error: Unrecognised generative distribution: " + name);

        try {
            Object[] initargs = new Object[arguments.keySet().size()];
            Constructor constructor = getConstructorByArguments(arguments, genDistClass, initargs);
            if (constructor == null)
                throw new RuntimeException("Parser error: no constructor found for generative distribution " + name + " with arguments " + arguments);

            GenerativeDistribution dist = (GenerativeDistribution) constructor.newInstance(initargs);
            for (String parameterName : arguments.keySet()) {
                Value value = dictionary.get(arguments.get(parameterName));
                dist.setParam(parameterName, value);
                globalArguments.add(value.id);
            }
            return dist;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing generative distribution " + name + " failed on line " + lineNumber);
        }
    }

    private Constructor getConstructorByArguments(Map<String, String> arguments, Class genDistClass, Object[] initargs) {
        System.out.println(arguments);
        for (Constructor constructor : genDistClass.getConstructors()) {
            List<ParameterInfo> pInfo = Parameterized.getParameterInfo(constructor);
            if (match(arguments, pInfo)) {
                for (int i = 0; i < pInfo.size(); i++) {
                    Value arg = dictionary.get(arguments.get(pInfo.get(i).name()));
                    if (arg == null)
                        throw new RuntimeException("Value for id=" + arguments.get(pInfo.get(i).name()) + " not found!");
                    initargs[i] = arg;
                    globalArguments.add(arg.id);
                }
                return constructor;
            }
        }
        return null;
    }

    private boolean match(Map<String, String> arguments, List<ParameterInfo> pInfo) {
        if (arguments.size() != pInfo.size()) return false;
        Set<String> paramSet = new TreeSet<>();
        for (ParameterInfo pi : pInfo) {
            paramSet.add(pi.name());
        }
        return paramSet.equals(arguments.keySet());
    }

    private Map<String, String> parseArguments(String argumentString, int lineNumber) {
        String[] argumentStrings = argumentString.split(",");
        TreeMap<String, String> arguments = new TreeMap<>();
        for (String argumentPair : argumentStrings) {
            if (argumentPair.indexOf('=') < 0) {
                argumentPair = "x=" + argumentPair;
            }
            String[] keyValue = argumentPair.split("=");
            if (keyValue.length != 2)
                throw new RuntimeException("Parsing argument " + keyValue[0].trim() + " failed on line " + lineNumber);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            arguments.put(key, value);
        }
        return arguments;
    }

    public static boolean isFunctionLine(String line) {
        int firstEquals = line.indexOf('=');
        if (firstEquals > 0) {
            String id = line.substring(0, firstEquals).trim();
            String remainder = line.substring(firstEquals + 1);
            return (remainder.indexOf('(') > 0);
        } else return false;
    }

    public static boolean isRandomVariableLine(String line) {
        return (line.indexOf('~') > 0);
    }

    public static boolean isFixedParameterLine(String line) {
        int firstEquals = line.indexOf('=');
        if (firstEquals > 0) {
            String id = line.substring(0, firstEquals).trim();

            String remainder = line.substring(firstEquals + 1);
            String valueString = remainder.substring(0, remainder.indexOf(';'));
            valueString = valueString.trim();

            if (valueString.startsWith("[") && valueString.endsWith("]")) {
                // is list
                return true;
            }

            try {
                Double val = Double.parseDouble(valueString);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        } else return false;
    }

    public static void main(String[] args) {
        String[] lines = {
                "kappa = 10.0;",
                "L = 50;",
                "mu = 0.01;",
                "n = 20;",
                "mean = 3.0;",
                "sd = 1.0;",
                "logTheta ~ Normal(mean=mean, sd=sd);",
                "Θ = exp(logTheta);",
                "Q = k80(kappa=kappa);",
                "ψ ~ Coalescent(n=n, theta=Θ);",
                "D ~ PhyloCTMC(L=L, mu=mu, Q=Q, tree=ψ);"};

        GraphicalModelParser parser = new GraphicalModelParser();
        parser.parseLines(lines);
        System.out.println(parser.dictionary);

    }

    public List<String> getLines() {
        return lines;
    }

    public void sample() {

        Set<String> sampled = new HashSet<>();

        for (Value value : getRoots()) {

            if (value instanceof RandomVariable) {
                RandomVariable variable = sampleAll(((RandomVariable) value).getGenerativeDistribution(), sampled);
                variable.setId(value.id);
                dictionary.put(variable.getId(), variable);
            }
        }
        notifyListeners();
    }

    private RandomVariable sampleAll(GenerativeDistribution generativeDistribution, Set<String> sampled) {

        for (Map.Entry<String, Value> e : getNewlySampledParams(generativeDistribution, sampled).entrySet()) {
            generativeDistribution.setParam(e.getKey(), e.getValue());
            sampled.add(e.getValue().getId());
        }

        return generativeDistribution.sample();
    }

    private Value sampleAll(DeterministicFunction function, Set<String> sampled) {

        for (Map.Entry<String, Value> e : getNewlySampledParams(function, sampled).entrySet()) {
            function.setParam(e.getKey(), e.getValue());
            sampled.add(e.getValue().getId());
        }

        return function.apply();
    }

    private Map<String, Value> getNewlySampledParams(Parameterized parameterized, Set<String> sampled) {
        Map<String, Value> params = parameterized.getParams();

        Map<String, Value> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {

            if (!sampled.contains(e.getValue().getId())) {
                // needs to be sampled

                if (e.getValue() instanceof RandomVariable) {
                    RandomVariable v = (RandomVariable) e.getValue();

                    RandomVariable nv = sampleAll(v.getGenerativeDistribution(), sampled);
                    nv.setId(v.getId());
                    newlySampledParams.put(e.getKey(), nv);
                    dictionary.put(nv.getId(), nv);
                } else if (e.getValue().getFunction() != null) {
                    Value v = e.getValue();
                    DeterministicFunction f = e.getValue().getFunction();

                    Value nv = sampleAll(f, sampled);
                    nv.setId(v.getId());
                    newlySampledParams.put(e.getKey(), nv);
                    dictionary.put(nv.getId(), nv);
                }
            } else {
                String id = e.getValue().getId();
                newlySampledParams.put(e.getKey(), dictionary.get(id));
            }
        }
        return newlySampledParams;
    }
}
