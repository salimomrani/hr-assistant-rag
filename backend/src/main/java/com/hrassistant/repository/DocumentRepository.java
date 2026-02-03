package com.hrassistant.repository;

import com.hrassistant.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    /**
     * Retrieves all distinct non-null categories.
     */
    @Query("SELECT DISTINCT d.category FROM Document d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findDistinctCategories();
}
