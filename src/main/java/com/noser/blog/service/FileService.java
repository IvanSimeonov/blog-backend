package com.noser.blog.service;

import com.noser.blog.domain.BlogFile;

public interface FileService {

  Long saveFile(final BlogFile file);
  BlogFile getFile(final Long id);
  Iterable<BlogFile> getFiles(final String name);
  boolean deleteFile(final Long id);
}
