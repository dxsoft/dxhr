package com.dxsoft.rsgzgl.dictionary;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    DictionaryService(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public PageResponse<DictionaryEntry> entries(String prefix, String keyword, PageRequest pageRequest) {
        return PageResponse.of(
                dictionaryRepository.findEntries(prefix, keyword, pageRequest),
                pageRequest,
                dictionaryRepository.countEntries(prefix, keyword));
    }
}
