package com.onelab.microservices.repository;

import com.onelab.microservices.model.Category;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface CategoryRepository extends ElasticsearchRepository<Category, String> {
    Optional<Category> findCategoryByName(String name);
}
