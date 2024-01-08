import core.em.PostgresEntityManager;
import core.em.Properties;
import entity.Person;

public class SimpleHibernateApp {

    public static void main(String[] args) {
        Properties property = new Properties("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
        PostgresEntityManager em = new PostgresEntityManager(property);
        Person person = em.find(Person.class, 1L);
        System.out.println(person);

        person = em.find(Person.class, 2L);
        System.out.println(person);

        person = em.find(Person.class, 3L);
        System.out.println(person);
    }
}
