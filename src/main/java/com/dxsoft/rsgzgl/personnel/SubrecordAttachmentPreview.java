package com.dxsoft.rsgzgl.personnel;

import org.springframework.core.io.Resource;

public record SubrecordAttachmentPreview(Resource resource, String fileName, String contentType) {
}
