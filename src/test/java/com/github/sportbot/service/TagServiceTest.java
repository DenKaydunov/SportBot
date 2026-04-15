package com.github.sportbot.service;

import com.github.sportbot.model.Tag;
import com.github.sportbot.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setCode("test-tag");
    }

    @Test
    void getIdByCode_whenTagExists_shouldReturnId() {
        // Given
        when(tagRepository.findByCode("test-tag")).thenReturn(Optional.of(testTag));

        // When
        Long result = tagService.getIdByCode("test-tag");

        // Then
        assertThat(result).isEqualTo(1L);
        verify(tagRepository).findByCode("test-tag");
    }

    @Test
    void getIdByCode_whenTagDoesNotExist_shouldReturnNull() {
        // Given
        when(tagRepository.findByCode("non-existing")).thenReturn(Optional.empty());

        // When
        Long result = tagService.getIdByCode("non-existing");

        // Then
        assertThat(result).isNull();
        verify(tagRepository).findByCode("non-existing");
    }
}
