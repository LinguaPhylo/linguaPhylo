package james.graphicalModel;

import java.io.PrintWriter;

/**
 * Created by adru001 on 18/12/19.
 */
public class RandomVariable<T> extends Value<T> {

    GenerativeDistribution<T> g;

    public RandomVariable(String name, T value, GenerativeDistribution<T> g) {
        super(name, value);
        this.g = g;
    }

    public GenerativeDistribution<T> getGenerativeDistribution() {
        return g;
    }

    public void print(PrintWriter p) {
        for (Value val : g.getParams()) {
            val.print(p);
            p.print("\n");
        }

        p.print(name + " ~ ");
        g.print(p);
        //p.print(";");
    }
}
