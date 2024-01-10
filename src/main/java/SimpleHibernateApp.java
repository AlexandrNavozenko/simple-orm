import core.em.PostgresEntityManager;
import core.em.Properties;
import entity.Person;

public class SimpleHibernateApp {

    public static void main(String[] args) {
        Properties property = new Properties("jdbc:postgresql://localhost:5432/postgres", "postgres", "root");
        PostgresEntityManager em = new PostgresEntityManager(property);
        System.out.println("==========");
        Person personOne = em.find(Person.class, 1L);
        System.out.println(personOne);

        Person personOne1 = em.find(Person.class, 1L);
        System.out.println(personOne1);

        Person personOne2 = em.find(Person.class, 1L);
        System.out.println(personOne2);

        System.out.println("==========");
        Person person = em.find(Person.class, 2L);
        System.out.println(person);

        person = em.find(Person.class, 3L);
        System.out.println(person);
        System.out.println("==========");
    }
}
