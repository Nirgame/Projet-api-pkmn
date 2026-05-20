package net.tcgdex.model;

import java.util.List;

/**
 * Représente une série Pokémon TCG
 */
public class Serie {
    private String id;
    private String name;
    private String logo;
    private List<Set> sets;

    public Serie() {
    }

    public Serie(String id, String name, String logo) {
        this.id = id;
        this.name = name;
        this.logo = logo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<Set> getSets() {
        return sets;
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    @Override
    public String toString() {
        return "Serie{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
