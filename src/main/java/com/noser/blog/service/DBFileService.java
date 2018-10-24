package com.noser.blog.service;

import com.noser.blog.domain.BlogFile;
import com.noser.blog.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DBFileService implements FileService {

  private final FileRepository fileRepository;

  public DBFileService(FileRepository fileRepository) {
    this.fileRepository = fileRepository;
  }

  @Override public Long saveFile(BlogFile file) {
    if (file == null || file.getData() == null || file.getName() == null) {
      return null;
    }

    file = fileRepository.save(file);
    return file.getId();
  }

  @Override
  public BlogFile getFile(Long id) {
    return fileRepository.findById(id).get();
  }

  @Override public List<BlogFile> getFiles(String name) {
    return null;
  }

  @Override
  public boolean deleteFile(Long id) {
    if (!fileRepository.existsById(id)) {
      return false;
    }

    fileRepository.deleteById(id);
    return true;
  }
}
