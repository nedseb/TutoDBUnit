package fr.univaix.iut.progbd;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class DAOPokemonJPATest {

    private static EntityManager entityManager;
    private static FlatXmlDataSet dataset;
    private static DatabaseConnection dbUnitConnection;
    private static EntityManagerFactory entityManagerFactory;

    private DAOPokemon dao = new DAOPokemonJPA(entityManager);

    @BeforeClass
    public static void initTestFixture() throws Exception {
        // Get the entity manager for the tests.
        entityManagerFactory = Persistence.createEntityManagerFactory("pokebattlePU");
        entityManager = entityManagerFactory.createEntityManager();

        Connection connection = ((EntityManagerImpl) (entityManager.getDelegate())).getServerSession().getAccessor().getConnection();

        dbUnitConnection = new DatabaseConnection(connection);
        //Loads the data set from a file
        dataset = new FlatXmlDataSetBuilder().build(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("pokemonDataset.xml"));
    }

    @AfterClass
    public static void finishTestFixture() throws Exception {
        entityManager.close();
        entityManagerFactory.close();
    }

    @Before
    public void setUp() throws Exception {
        //Clean the data from previous test and insert new data test.
        DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, dataset);
    }

    @Test
    public void testFindByType() throws Exception {
        List<Pokemon> pokemons = dao.findByType(Type.ELECTRIC);
        assertThat(pokemons.get(0).getName()).isEqualTo("Pikachu");
    }

    @Test
    public void testFindAll() throws Exception {
        List<Pokemon> pokemons = dao.findAll();
        assertThat(pokemons.get(0).getName()).isEqualTo("Pikachu");
        assertThat(pokemons.get(1).getName()).isEqualTo("Rattata");
    }

    @Test
    public void testGetById() throws Exception {
        assertThat(dao.getById("Pikachu").getName()).isEqualTo("Pikachu");
        assertThat(dao.getById("Rattata").getName()).isEqualTo("Rattata");
    }

    @Test
    public void testDelete() throws Exception {
        dao.delete(dao.getById("Pikachu"));
        assertThat(dao.getById("Pikachu")).isNull();
    }

    @Test
    public void testInsert() throws Exception {
        Pokemon raichu = new Pokemon("Raichu");
        raichu.setType1(Type.ELECTRIC);
        dao.insert(raichu);
        assertThat(dao.getById("Raichu").getName()).isEqualTo("Raichu");
        assertThat(dao.getById("Raichu").getType1()).isEqualTo(Type.ELECTRIC);
    }

    @Test
    public void testUpdate() throws Exception {
        Pokemon pikachu = dao.getById("Pikachu");
        assertThat(pikachu.getAttack()).isGreaterThan(0);
        pikachu.setAttack(-1);
        dao.update(pikachu);
        assertThat(dao.getById("Pikachu").getAttack()).isLessThan(0);
    }
}
