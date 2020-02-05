package james.graphicalModel;

import james.Coalescent;
import james.core.JCPhyloCTMC;
import james.core.distributions.Exp;
import james.core.distributions.LogNormal;
import james.core.distributions.Normal;
import james.swing.GraphicalModelChangeListener;
import james.swing.GraphicalModelListener;

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

    public GraphicalModelParser() {
        genDistDictionary.put("Normal", Normal.class);
        genDistDictionary.put("LogNormal", LogNormal.class);
        genDistDictionary.put("Exp", Exp.class);
        genDistDictionary.put("Coalescent", Coalescent.class);
        genDistDictionary.put("JCPhyloCTMC", JCPhyloCTMC.class);

        System.out.println(genDistDictionary);

        functionDictionary.put("exp", james.core.functions.Exp.class);
    }

    public void addGraphicalModelChangeListener(GraphicalModelChangeListener listener) {
        listeners.add(listener);
    }

    public SortedMap<String, Value> getDictionary() {
        return dictionary;
    }

    public SortedSet<Value> getRoots() {
        SortedSet<String> nonArguments = new TreeSet<>(dictionary.keySet());
        nonArguments.removeAll(globalArguments);
        SortedSet<Value> values = new TreeSet<>();
        for (String id : nonArguments) {
            values.add(dictionary.get(id));
        }
        return values;
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
        } else throw new RuntimeException("Parse error on line " + lineNumber + ": " + line);
        lines.add(line);
        notifyListeners();
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
        if (parts.length != 2) throw new RuntimeException("Parsing fixed parameter " + parts[0] + " failed on line " + lineNumber);
        String id = parts[0].trim();
        String valueString = parts[1].substring(0,parts[1].indexOf(';')).trim();
        Value literalValue = parseLiteralValue(id, valueString, lineNumber);
        dictionary.put(literalValue.getId(), literalValue);
    }

    private Value parseLiteralValue(String id, String valueString, int lineNumber) {

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

        throw new RuntimeException("Parsing fixed parameter " + id + " with value "  + valueString + " failed on line " + lineNumber);
    }

    private void parseFunctionLine(String line, int lineNumber) {
        int firstEquals = line.indexOf('=');
        String id = line.substring(0, firstEquals).trim();
        String remainder = line.substring(firstEquals+1);
        String functionString = remainder.substring(0,remainder.indexOf(';'));
        Value val = parseFunction(id, functionString, lineNumber);
        dictionary.put(val.getId(), val);
    }

    private Value parseFunction(String id, String functionString, int lineNumber) {
        String[] parts = functionString.split("\\(");
        if (parts.length != 2) throw new RuntimeException("Parsing function " + parts[0] + "failed on line " + lineNumber);
        String funcName = parts[0].trim();
        String argumentString = parts[1].substring(0,parts[1].indexOf(')')).trim();
        Value argument = dictionary.get(argumentString);
        if (argument == null) {
            System.err.println(dictionary);
            throw new RuntimeException("Couldn't find argument " + argumentString + " when parsing " + funcName + " on line " + lineNumber);
        } else {
            globalArguments.add(argumentString);
        }

        Class functionClass = functionDictionary.get(funcName);
        try {
            Function function = (Function)functionClass.newInstance();
            Value result = function.apply(argument, id);
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing function " + funcName + " failed on line " + lineNumber);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing function " + funcName + " failed on line " + lineNumber);
        }
    }

    private void parseRandomVariable(String line, int lineNumber) {
        String[] parts = line.split("~");
        if (parts.length != 2) throw new RuntimeException("Parsing random variable " + parts[0] + "failed on line " + lineNumber);
        String id = parts[0].trim();
        String genString = parts[1].substring(0,parts[1].indexOf(';'));
        GenerativeDistribution genDist = parseGenDist(genString, lineNumber);
        RandomVariable var = genDist.sample(id);
        dictionary.put(var.getId(), var);
    }

    private GenerativeDistribution parseGenDist(String genString, int lineNumber) {
        String[] parts = genString.split("\\(");
        if (parts.length != 2) throw new RuntimeException("Parsing generative distribution " + parts[0] + "failed on line " + lineNumber);
        String name = parts[0].trim();
        String argumentString = parts[1].substring(0,parts[1].indexOf(')'));
        Map<String,String> arguments = parseArguments(argumentString, lineNumber);

        Class genDistClass = genDistDictionary.get(name);
        if (genDistClass == null) throw new RuntimeException("Parsing error: Unrecognised generative distribution: " + name);

        try {
            Object[] initargs = new Object[arguments.keySet().size()];
            Constructor constructor = getConstructorByArguments(arguments, genDistClass, initargs);
            if (constructor == null) throw new RuntimeException("Parser error: no constructor found for generative distribution " + name + " with arguments " + arguments);

            GenerativeDistribution dist = (GenerativeDistribution)constructor.newInstance(initargs);
            for (String parameterName : arguments.keySet()) {
                Value value = dictionary.get(arguments.get(parameterName));
                dist.setParam(parameterName, value);
                globalArguments.add(value.getId());
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
                    if (arg == null) throw new RuntimeException("Value for id=" + arguments.get(pInfo.get(i).name()) + " not found!");
                    initargs[i] = arg;
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
            String[] keyValue = argumentPair.split("=");
            if (keyValue.length != 2) throw new RuntimeException("Parsing argument " + keyValue[0].trim() + "failed on line " + lineNumber);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            arguments.put(key, value);
        }
        return arguments;
    }

    public static boolean isFunctionLine(String line) {
        int firstEquals = line.indexOf('=');
        if (firstEquals>0) {
            String id = line.substring(0, firstEquals).trim();
            String remainder = line.substring(firstEquals+1);
            return (remainder.indexOf('(')>0);
        } else return false;
    }

    public static boolean isRandomVariableLine(String line) {
        return (line.indexOf('~') > 0);
    }

    public static boolean isFixedParameterLine(String line) {
        int firstEquals = line.indexOf('=');
        String id = line.substring(0, firstEquals).trim();
        String remainder = line.substring(firstEquals+1);
        String valueString = remainder.substring(0, remainder.indexOf(';'));

        try {
            Double val = Double.parseDouble(valueString);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static void main(String[] args) {
        String[] lines = {
                "L = 50;",
                "dim = 4;",
                "mu = 0.01;",
                "n = 20;",
                "mean = 3.0;",
                "sd = 1.0;",
                "logTheta ~ Normal(μ=mean, σ=sd);",
                "Θ = exp(logTheta);",
                "ψ ~ Coalescent(n=n, theta=Θ);",
                "D ~ JCPhyloCTMC(L=L, dim=dim, mu=mu, tree=ψ);"};

        GraphicalModelParser parser = new GraphicalModelParser();
        parser.parseLines(lines);
        System.out.println(parser.dictionary);

        Set<String> nonArguments = parser.dictionary.keySet();
        nonArguments.removeAll(parser.globalArguments);
        System.out.println("Non-arguments = " + parser.dictionary);
        System.out.println("Found " + nonArguments.size() + " root values");

    }

    public List<String> getLines() {
        return lines;
    }

    public void sample() {

        for (Value value : getRoots()) {

            if (value instanceof RandomVariable) {
                RandomVariable variable = sampleAll(((RandomVariable)value).getGenerativeDistribution());
                variable.setId(value.id);
                dictionary.put(variable.getId(), variable);
            }
        }
        notifyListeners();
    }

    private RandomVariable sampleAll(GenerativeDistribution generativeDistribution) {
        Map<String, Value> params = generativeDistribution.getParams();

        Map<String, Value> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            if (e.getValue() instanceof RandomVariable) {
                RandomVariable v = (RandomVariable) e.getValue();

                RandomVariable nv = sampleAll(v.getGenerativeDistribution());
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
                dictionary.put(nv.getId(), nv);
            } else if (e.getValue().getFunction() != null) {
                Value v = e.getValue();
                Function f = e.getValue().getFunction();

                Value nv = sampleAll(f);
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
                dictionary.put(nv.getId(), nv);

            }
        }
        for (Map.Entry<String, Value> e : newlySampledParams.entrySet()) {
            generativeDistribution.setParam(e.getKey(), e.getValue());
        }

        return generativeDistribution.sample();
    }

    private Value sampleAll(Function function) {
        Map<String, Value> params = function.getParams();

        Map<String, RandomVariable> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            if (e.getValue() instanceof RandomVariable) {
                RandomVariable v = (RandomVariable) e.getValue();

                RandomVariable nv = sampleAll(v.getGenerativeDistribution());
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
                dictionary.put(nv.getId(), nv);
            } else if (e.getValue().getFunction() != null) {
                Value v = e.getValue();
                Function f = e.getValue().getFunction();

                Value nv = sampleAll(f);
                nv.setId(v.getId());
                dictionary.put(nv.getId(), nv);
            }
        }
        return (Value) function.apply(newlySampledParams.entrySet().iterator().next().getValue());

    }
}
