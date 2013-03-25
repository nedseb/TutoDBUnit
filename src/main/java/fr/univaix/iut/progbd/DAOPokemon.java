package fr.univaix.iut.progbd;

import java.util.List;

public interface DAOPokemon extends DAO<Pokemon, String> {
    public List<Pokemon> findByType(Type type);
}
