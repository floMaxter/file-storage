package com.projects.filestorage.web.dto.internal.enums;

public record ResourceLocationDto (String bucket,
                                   String rootDirectory,
                                   String absolutePath) {
}
