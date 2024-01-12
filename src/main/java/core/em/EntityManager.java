package core.em;

public interface EntityManager {

    <T> T find(Class<T> clazz, Long id);
}
