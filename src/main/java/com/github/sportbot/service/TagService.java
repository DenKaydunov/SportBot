package com.github.sportbot.service;

import com.github.sportbot.model.Tag;
import com.github.sportbot.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Long getIdByCode(String tagCode) {
        Optional<Tag> tag = tagRepository.findByCode(tagCode);
        return tag.map(Tag::getId).orElse(null);
    }
}
