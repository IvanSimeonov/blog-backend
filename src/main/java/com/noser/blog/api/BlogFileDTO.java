package com.noser.blog.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BlogFileDTO {
  private long id;
  private String name;
  private long size;
  private boolean deletable;

}
