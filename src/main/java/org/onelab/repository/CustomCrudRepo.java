package org.onelab.repository;

import java.util.List;
import java.util.Optional;

public interface CustomCrudRepo<T> {

    List<T> findAll();

    void save(T t);

    Optional<T> findById(int id);

    void update(T t, int id);

    void remove(int id);
}
