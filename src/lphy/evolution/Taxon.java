package lphy.evolution;

public class Taxon { // implements Attributable

    private final String name;
    private String species;
    private double age;

    public Taxon(String name) {
        this (name, null, 0.0);
    }

    public Taxon(String name, double age) {
        this(name, null, age);
    }

    public Taxon(String name, String species) {
        this(name, species, 0.0);
    }

    public Taxon(String name, String species, double age) {
        this.name = name;
        this.species = species;
        this.age = age;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public double getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        if (species == null) return name;
        return species;
    }

    public String toString() {
        return name + ((species != null && !species.equals(name)) ? "/" + species : "") + "=" + age;
    }
}
