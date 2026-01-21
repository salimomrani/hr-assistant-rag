package com.hrassistant.mapper;

import com.hrassistant.model.Document;
import com.hrassistant.model.DocumentInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Document entity to DocumentInfo DTO conversion.
 *
 * Usage:
 * <pre>
 * DocumentInfo info = documentMapper.toDocumentInfo(document);
 * </pre>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapper {

    /**
     * Converts Document entity to DocumentInfo DTO.
     *
     * @param document The document entity
     * @return DocumentInfo DTO with all fields mapped
     */
    DocumentInfo toDocumentInfo(Document document);
}
