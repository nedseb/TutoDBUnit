package fr.univaix.iut.progbd;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = Pokemon.FIND_ALL, query = "SELECT p FROM Pokemon p"),
        @NamedQuery(name = Pokemon.FIND_BY_TYPE, query = "SELECT p FROM Pokemon p WHERE p.name = :ftype")
})
public class Pokemon {
    public static final String FIND_BY_TYPE = "findPokemonByType";
    public static final String FIND_ALL = "findAllPokemon";
    @Id
    private String name;

    @Enumerated(EnumType.STRING)
    private Type types1;

    @Enumerated(EnumType.STRING)
    private Type types2;

    private int baseHP;
    private int attack;
    private int defense;
    private int attackSpecial;
    private int defenseSpecial;
    private int speed;

    protected Pokemon() {

    }

    public Pokemon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getTypes1() {
        return types1;
    }

    public void setTypes1(Type types1) {
        this.types1 = types1;
    }

    public Type getTypes2() {
        return types2;
    }

    public void setTypes2(Type types2) {
        this.types2 = types2;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public void setBaseHP(int baseHP) {
        this.baseHP = baseHP;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getAttackSpecial() {
        return attackSpecial;
    }

    public void setAttackSpecial(int attackSpecial) {
        this.attackSpecial = attackSpecial;
    }

    public int getDefenseSpecial() {
        return defenseSpecial;
    }

    public void setDefenseSpecial(int defenseSpecial) {
        this.defenseSpecial = defenseSpecial;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokemon)) return false;

        Pokemon pokemon = (Pokemon) o;

        if (attack != pokemon.attack) return false;
        if (attackSpecial != pokemon.attackSpecial) return false;
        if (baseHP != pokemon.baseHP) return false;
        if (defense != pokemon.defense) return false;
        if (defenseSpecial != pokemon.defenseSpecial) return false;
        if (speed != pokemon.speed) return false;
        if (name != null ? !name.equals(pokemon.name) : pokemon.name != null) return false;
        if (types1 != pokemon.types1) return false;
        if (types2 != pokemon.types2) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (types1 != null ? types1.hashCode() : 0);
        result = 31 * result + (types2 != null ? types2.hashCode() : 0);
        result = 31 * result + baseHP;
        result = 31 * result + attack;
        result = 31 * result + defense;
        result = 31 * result + attackSpecial;
        result = 31 * result + defenseSpecial;
        result = 31 * result + speed;
        return result;
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "name='" + name + '\'' +
                ", types1=" + types1 +
                ", types2=" + types2 +
                ", baseHP=" + baseHP +
                ", attack=" + attack +
                ", defense=" + defense +
                ", attackSpecial=" + attackSpecial +
                ", defenseSpecial=" + defenseSpecial +
                ", speed=" + speed +
                '}';
    }
}
