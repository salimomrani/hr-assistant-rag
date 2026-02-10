package com.hrassistant.repository;

import com.hrassistant.model.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

  /** Retrieves all distinct non-null categories. */
  @Query(
      "SELECT DISTINCT d.category FROM Document d WHERE d.category IS NOT NULL ORDER BY d.category")
  List<String> findDistinctCategories();

  List<Document> findByFilenameIn(List<String> filenames);
}
