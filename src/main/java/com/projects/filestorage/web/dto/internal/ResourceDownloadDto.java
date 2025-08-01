package com.projects.filestorage.web.dto.internal;

import lombok.Builder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Builder
public record ResourceDownloadDto(String fileName,
                                  StreamingResponseBody responseBody) {
}
