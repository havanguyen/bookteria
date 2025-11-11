package com.hanguyen.search_service.controller;

import com.hanguyen.search_service.document.ProductDocument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class SearchController {

    ElasticsearchOperations elasticsearchOperations;

    @GetMapping("/product")
    public Page<ProductDocument> searchProducts(
            @RequestParam(value = "q", required = false) String keyword,
            Pageable pageable
    ) {
        log.info("🔍 Searching products for keyword: '{}'", keyword);

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withPageable(pageable);

        if (StringUtils.hasText(keyword)) {

            String keywordWithWildcard = keyword.toLowerCase() + "*";

            log.info("Building multi_match query for keyword: {}", keywordWithWildcard);
            queryBuilder.withQuery(q -> q
                    .queryString(m -> m
                            .query(keywordWithWildcard)
                            .fields("title^3", "description", "authorName", "publisherName")
                            .fuzziness("AUTO")
                    )
            );
        } else {
            log.info("Building match_all query");
            queryBuilder.withQuery(q -> q
                    .matchAll(m -> m)
            );
        }

        Query query = queryBuilder.build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        SearchPage<ProductDocument> searchPage = SearchHitSupport.searchPageFor(hits, pageable);
        @SuppressWarnings("unchecked")
        Page<ProductDocument> page = (Page<ProductDocument>) SearchHitSupport.unwrapSearchHits(searchPage);
        return page;
    }
}