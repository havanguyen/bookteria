package com.hanguyen.identity.dto.response;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    List<UserResponse> content;
    int pageNo;
    int pageSize;
    long totalElements;
    int totalPages;
    boolean last;
}
