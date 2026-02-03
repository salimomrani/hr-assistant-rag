package com.hrassistant.mapper;

import com.hrassistant.model.Document;
import com.hrassistant.model.DocumentInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

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
    @Mapping(target = "hasFile", source = "filePath", qualifiedByName = "filePathToHasFile")
    DocumentInfo toDocumentInfo(Document document);

    /**
     * Converts filePath to hasFile boolean.
     *
     * @param filePath The file path
     * @return true if file path is not empty
     */
    @Named("filePathToHasFile")
    default Boolean filePathToHasFile(String filePath) {
        return StringUtils.hasText(filePath);
    }
}
