package com.Movieflix.dto;

import java.util.List;

public record MoviePageResponse(List<MovieDto> movieDtos,Integer pageNumber,
                                Integer pagesize , int totalElements , int totalPages,
                                boolean isLast) {
}
