package fr.univaix.iut.progbd;

import java.util.List;

public interface DAOPokemon extends DAO<Pokemon, String> {
    public List<Pokemon> FindByType(Type type);
}
