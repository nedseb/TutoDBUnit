#TutoDBUnit

Ce tutoriel présente une manière de mettre en place des tests unitaires 
pour les entités et les DAO d'une couche de persistance construite avec JPA.

Le test unitaire d'une classe de la couche de persistance differe de celui 
d'une classe classique. Les objets de ces classes ont besoin d'interagir avec 
le SGBD-R. Le SGBD étant généralement un serveur externe à notre application, 
il faudrait donc que le poste du développeur ait accès à ce serveur. Cette 
dépendance à un programme externe est contraire au principe FIRST (Fast, 
Independant, Repeteable, Self-Verifying, Timely). Le risque de conserver 
cette dépendance dans l'environement de test est que tous les développeurs 
ne pourront pas nécéssairement lancer la suite de test régulièrement. En 
plus pour que les tests soient répétables, il faudrait réinitialiser l'état 
de la base avant chaque méthode de test.

Pour solutionner ces problème nous allons découvrir deux outils. Le premier 
Derby (http://db.apache.org/derby/) est un moteur de base de données écrit 
en Java. Étant multi-plateforme de petite taille(2Mo), ce moteur peut 
facilement être intégré directement au sein d'une application Java. L'un des 
modes de fonctionnement de cette base de données est purement en mémoire. Ainsi 
la base aura la même durée de vie que le programme. Ce mode de fonctionnement 
est particulièrement intéréssant dans le cas des tests unitaires car il élimine 
le besoin d'un serveur externe et qu'il permet une maitrise totale des données.

Le second outil que l'on va utiliser sera DBUnit (http://www.dbunit.org/). Cet 
outil est un complément à JUnit pour les projets centrés sur une Base de données.
Il permet entre autre chose de remettre la BD dans un état connu entre le lancement 
de chaque test. Il permet aussi de définir le jeu de test dans un fichier XML simple.

##Création du projet
Dans ce tutoriel nous allons supposer que notre projet est géré avec Maven. Vous trouverez 
le code complet de ce tutoriel sur directement Github (https://github.com/nedseb/TutoDBUnit).

Commençons par définir notre unique entité `Pokemon`:
```Java
package fr.univaix.iut.progbd;

import javax.persistence.*;

@Entity
@NamedQueries({
   @NamedQuery(name = Pokemon.FIND_ALL, query = "SELECT p FROM Pokemon p"),
   @NamedQuery(name = Pokemon.FIND_BY_TYPE, query = "SELECT p FROM Pokemon p WHERE p.name = :ftype"),
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

    protected Pokemon(){

    }

    public Pokemon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
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
```
En plus de cette entité nous avons aussi besoin d'une énumeration `Type` qui décrit les types des pokémons :
```Java
package fr.univaix.iut.progbd;

public enum Type {
    NORMAL,
    FIRE,
    FIGHTING,
    WATER,
    FLYING,
    GRASS,
    POISON,
    ELECTRIC,
    GROUND,
    PSYCHIC,
    ROCK,
    ICE,
    BUG,
    DRAGON,
    GHOST,
    DARK,
    STEEL
}
```
Notre couche métier ne contiendra que cette classe. Écrivons maintenant les DAO de notre couche de persistance.
Rajoutons tout d'abord l'interface d'un DAO :
```Java
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
    public List<T> FindAll();

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
```
Puis l'interface de notre seule DAO `DAOPokemon` :
```Java
package fr.univaix.iut.progbd;

import java.util.List;

public interface DAOPokemon extends DAO<Pokemon, String> {
    public List<Pokemon> FindByType(Type type);
}
```
Et enfin la classe `DAOPokemonJPA` implémentant cette interface en utilisant JPA :
```Java
package fr.univaix.iut.progbd;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

public class DAOPokemonJPA implements DAOPokemon {

    private EntityManager entityManager;

    public DAOPokemonJPA(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Pokemon> FindByType(Type type) {
        TypedQuery<Pokemon> query = entityManager.createNamedQuery(Pokemon.FIND_BY_TYPE, Pokemon.class);
        query.setParameter("ftype", type);
        return query.getResultList();
    }

    @Override
    public boolean delete(Pokemon obj) {
        try {
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();
            entityManager.remove(obj);
            tx.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Pokemon> FindAll() {
        TypedQuery<Pokemon> query = entityManager.createNamedQuery(Pokemon.FIND_ALL, Pokemon.class);
        return query.getResultList();
    }

    @Override
    public Pokemon getById(String id) {
        return entityManager.find(Pokemon.class, id);
    }

    @Override
    public Pokemon insert(Pokemon obj) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        entityManager.persist(obj);
        tx.commit();
        return entityManager.find(Pokemon.class, obj.getName());
    }

    @Override
    public boolean update(Pokemon obj) {
        try {
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();
            entityManager.merge(obj);
            tx.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```
#Configuration de la persistence

Le projet principal utilise MySQL. Pour configurer JPA on utilise le fichier `src/main/resources/META-INF/persistence.xml` suivant :
```XML
<?xml version="1.0" encoding="UTF-8" ?>

<persistence xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd" version="2.0" xmlns="http://java.sun.com/xml/ns/persistence">
  <persistence-unit name="pokebattlePU" transaction-type="RESOURCE_LOCAL">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    <class>fr.univaix.iut.progbd.Pokemon</class>
    <properties>
        <property name="eclipselink.target-database" value="MySQL" />
        <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/PokemonDB"/>
        <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver"/>
        <property name="javax.persistence.jdbc.user"  value="monUser"/>
        <property name="javax.persistence.jdbc.password"  value="monPassword"/>
        <property name="eclipselink.logging.level" value="INFO" />
        <property name="eclipselink.ddl-generation"  value="create-tables"/>
    </properties>
  </persistence-unit>
</persistence>
``` 

Le fichier`POM` de notre projet est le suivant :
```XML
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>fr.univaix.iut.progbd</groupId>
  <artifactId>TutoDBUnit</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>TutoDBUnit</name>
  <url>http://maven.apache.org</url>

  <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<maven.compiler.source>1.6</maven.compiler.source>
		<maven.compiler.target>1.6</maven.compiler.target>
	</properties>

	<dependencies>
		<dependency>
		    <groupId>junit</groupId>
		    <artifactId>junit</artifactId>
		    <version>4.11</version>
		    <scope>test</scope>
		</dependency>

		<dependency>
		    <groupId>org.easytesting</groupId>
		    <artifactId>fest-assert</artifactId>
		    <version>1.4</version>
		    <scope>test</scope>
		</dependency>

		<dependency>
		    <groupId>org.eclipse.persistence</groupId>
		    <artifactId>javax.persistence</artifactId>
		    <version>2.0.0</version>
		</dependency>

		<dependency>
		    <groupId>org.eclipse.persistence</groupId>
		    <artifactId>eclipselink</artifactId>
		    <version>2.4.1</version>
		</dependency>

		<dependency>
		    <groupId>mysql</groupId>
		    <artifactId>mysql-connector-java</artifactId>
		    <version>5.1.23</version>
		</dependency>
	</dependencies>

	<repositories>
		<repository>
			<id>EclipseLink Repo</id>
			<url>http://www.eclipse.org/downloads/download.php?r=1&amp;nf=1&amp;file=/rt/eclipselink/maven.repo</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>
</project>
```
A ce point de ce tutoriel nous avons une application JPA tout à fait classique 
comme nous en avons déjà crée pendant les TP JPA (https://github.com/nedseb/TpJPA et 
https://github.com/nedseb/SqueletteTpJPA). Pour vérifier que notre projet a été correctement 
crée et configuré nous ajoutons la classe suivante qui contient une méthode `public static void main(String[] args)` :
```Java
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
        pikachu.setTypes1(Type.ELECTRIC);
        em.persist(pikachu);

        em.getTransaction().commit();

        em.close();
        emf.close();
    }
}
```
##Configuration de Derby
Pour que notre suite de test utilise une base de données différente de celle de notre programme principal, 
il utiliser la notion de `scope` de Maven. Toutes les dépendances à Derby devront donc comporter la balise 
`<scope>test</scope>`. Ainsi ces dépendances ne seront accessible uniquement aux classes situées dans le 
dossier `/src/test/`. Les dépendances à rajouter à notre fichier `pom.xml` sont les suivantes : 
```XML
<dependency>
  <groupId>org.apache.derby</groupId>
  <artifactId>derby</artifactId>
  <version>10.9.1.0</version>
  <scope>test</scope>
</dependency>   
<dependency>
  <groupId>org.apache.derby</groupId>
  <artifactId>derbyclient</artifactId>
  <version>10.9.1.0</version>
  <scope>test</scope>
</dependency>
```
De même que les dépendances, il nous faut un fichier de configuration propre à notre suite de test. Pour se faire 
il suffit de créer un nouveau fichier `persistence.xml` que l'on placera dans le dossier `/src/test/resources/META-INF`.
Pour satisfaire notre besoin il faut utiliser le connecteur JDBC embarqué `org.apache.derby.jdbc.EmbeddedDriver` en même 
temps qu'une URL de connection spécifiant que l'on utilise une BD qui résidera en mémoire (`jdbc:derby:memory`). 
Le fichier `persistence.xml` sera le suivant :
```XML
<?xml version="1.0" encoding="UTF-8" ?>

<persistence xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
             version="2.0" xmlns="http://java.sun.com/xml/ns/persistence">
  <persistence-unit name="pokebattlePU" transaction-type="RESOURCE_LOCAL">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    <class>fr.univaix.iut.progbd.Pokemon</class>
    <properties>
        <property name="eclipselink.target-database" value="Derby" />
        <property name="javax.persistence.jdbc.driver" value="org.apache.derby.jdbc.EmbeddedDriver" />
        <property name="javax.persistence.jdbc.url" value="jdbc:derby:memory:PokemonDB;create=true" />
        <property name="javax.persistence.jdbc.user" value="" />
        <property name="javax.persistence.jdbc.password" value="" />
        <property name="eclipselink.logging.level" value="INFO" />
        <property name="eclipselink.ddl-generation"  value="create-tables"/>
    </properties>
  </persistence-unit>
</persistence>
```
##Configuration de DBUnit
DBUnit est le complément à JUnit qui va nous permettre de maitriser le remplissage ainsi que le netoyage 
de la base de données entre les différents tests. La première chose à faire pour l'utiliser est de rajouter 
les dépendances suivantes dans le fichier `pom.xml` :
```XML
<dependency>
    <groupId>org.dbunit</groupId>
    <artifactId>dbunit</artifactId>
    <version>2.4.9</version>
    <scope>test</scope>
</dependency>
```

Les jeux d'essais qui seront utilisés pour remplir la base de données sont des fichiers XML qu'il 
faudra placer dans le dossier des ressources de test : `src/test/resources/`. Pour tester notre DAO, 
nous utiliserons le jeu d'essai suivant :
```XML
<dataset>
    <table name="POKEMON">
        <column>NAME</column>
        <column>TYPE1</column>
        <column>TYPE2</column>
        <column>BASEHP</column>
        <column>ATTACK</column>
        <column>DEFENSE</column>
        <column>ATTACKSPECIAL</column>
        <column>DEFENCESPECIAL</column>
        <column>SPEED</column>
        <row>
            <value>Pikachu</value>
            <value>ELECTRIC</value>
            <null/>
            <value>35</value>
            <value>55</value>
            <value>30</value>
            <value>50</value>
            <value>40</value>
            <value>90</value>
        </row>
        <row>
            <value>Rattata</value>
            <value>NORMAL</value>
            <null/>
            <value>30</value>
            <value>56</value>
            <value>35</value>
            <value>25</value>
            <value>35</value>
            <value>72</value>
        </row>
    </table>
</dataset>
```


