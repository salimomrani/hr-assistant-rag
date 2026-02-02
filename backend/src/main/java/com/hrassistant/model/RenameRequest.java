package com.hrassistant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for renaming a document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameRequest {

    @NotBlank(message = "New filename is required")
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    private String newFilename;
}
