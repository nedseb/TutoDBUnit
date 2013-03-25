package fr.univaix.iut.progbd;

import java.util.List;

public interface DAO<T, ID> {

    /**
     * Permet la suppression d'un tuple de la base
     *
     * @param obj
     */
    public boolean delete(T obj);

    /**
     * Permet de récupérer tous les objets d'une table
     *
     * @return
     */
    public List<T> findAll();

    /**
     * Permet de récupérer un objet via son ID
     *
     * @param id
     * @return
     */
    public T getById(ID id);

    /**
     * Permet de créer une entrée dans la base de données par rapport à un objet
     *
     * @param obj
     */
    public T insert(T obj);

    /**
     * Permet de mettre à jour les données d'un tuple dans la base à partir d'un
     * objet passé en paramètre
     *
     * @param obj
     */
    public boolean update(T obj);

}