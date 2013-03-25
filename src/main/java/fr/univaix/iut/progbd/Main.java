package fr.univaix.iut.progbd;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {

    public static void main(String[] args) {
        // Initializes the Entity manager

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pokebattlePU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Pokemon pikachu = new Pokemon("Pikachu");
        pikachu.setType1(Type.ELECTRIC);
        em.persist(pikachu);

        em.getTransaction().commit();

        em.close();
        emf.close();
    }
}
