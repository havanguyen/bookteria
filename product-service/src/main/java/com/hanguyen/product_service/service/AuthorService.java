package com.hanguyen.product_service.service;

import com.hanguyen.product_service.dto.request.AuthorRequest;
import com.hanguyen.product_service.dto.response.AuthorResponse;
import com.hanguyen.product_service.entity.Author;
import com.hanguyen.product_service.exception.AppException;
import com.hanguyen.product_service.exception.ErrorCode;
import com.hanguyen.product_service.mapper.AuthorMapper;
import com.hanguyen.product_service.repository.AuthorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthorService {
    AuthorRepository authorRepository;
    AuthorMapper authorMapper;

    @Transactional
    public AuthorResponse createAuthor(AuthorRequest authorRequest){
        Author author = authorMapper.toAuthor(authorRequest);
        author = authorRepository.save(author);
        return  authorMapper.toAuthorResponse(author);
    }

    @Transactional
    public AuthorResponse updateAuthor(String id ,AuthorRequest authorRequest){
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));

        author.setBio(authorRequest.getBio());
        author.setName(authorRequest.getName());

        author = authorRepository.save(author);

        return authorMapper.toAuthorResponse(author);
    }

    public Page<AuthorResponse> getAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(authorMapper::toAuthorResponse);
    }

    public AuthorResponse getById(String id) {
        return authorRepository.findById(id)
                .map(authorMapper::toAuthorResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
    }

    public void delete(String id) {
        authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
        authorRepository.deleteById(id);
    }
}
