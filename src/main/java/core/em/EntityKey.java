package core.em;

public record EntityKey<T>(Class<T> clazz, Long id) {
}
