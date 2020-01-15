package com.noser.blog.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import com.noser.blog.api.BlogFilePageDTO;
import org.apache.http.entity.ContentType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.noser.blog.api.BlogFileCollectionDTO;
import com.noser.blog.api.BlogFileDTO;
import com.noser.blog.domain.BlogFile;
import com.noser.blog.domain.Thumbnail;
import com.noser.blog.mapper.FileMapper;
import com.noser.blog.service.FileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class FileController {
	
	

  private final FileService fileService;
  private final FileMapper fileMapper;
  
  public FileController(FileService fileService, FileMapper fileMapper) {
    this.fileService = fileService;
    this.fileMapper = fileMapper;
  }

  @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody BlogFileDTO uploadFile(@RequestParam("file") MultipartFile file, Principal principal, Authentication authentication) throws Exception {
    log.info("File uploaded: {}", file.getOriginalFilename());

    final BlogFile blogFile = BlogFile.builder()
        .name(file.getOriginalFilename())
        .authorId(principal != null ? principal.getName(): null)
        .data(file.getBytes())
        .uploaded(LocalDateTime.now())
        .size(file.getSize())
        .fileType(file.getContentType())
        .build();
    blogFile.setId(this.fileService.saveFile(blogFile));
    return this.fileMapper.domain2dto(blogFile);
  }

  @Cacheable("images")
  @GetMapping(value = "/files/{id}")
  public @ResponseBody ResponseEntity<byte[]> getFile(@PathVariable Long id, @RequestParam Optional<Integer> size) throws IOException {
    final BlogFile file = this.fileService.getFile(id);
    final HttpHeaders httpHeaders = new HttpHeaders();

    if (file == null) {
      return new ResponseEntity<>(null, httpHeaders, HttpStatus.NOT_FOUND);
    }

    if (size.isPresent()) {
      final Thumbnail thumbnail = fileService.getThumbnail(file, size.get());
      if (thumbnail.getData() != null) {
    	  httpHeaders.add("Content-Type", ContentType.IMAGE_JPEG.getMimeType());
    	  return new ResponseEntity<>(thumbnail.getData(), httpHeaders, HttpStatus.OK);
      }
    }

    httpHeaders.add("Content-Type", ContentType.getByMimeType(file.getFileType()).toString());
    return new ResponseEntity<>(file.getData(), httpHeaders, HttpStatus.OK);
  }

  @GetMapping(value = "/files")
  public @ResponseBody BlogFileCollectionDTO getFiles() {
    final BlogFileCollectionDTO blogFileCollectionDTO = BlogFileCollectionDTO.builder().blogFiles(new ArrayList<>()).build();
    this.fileService.getFiles(null)
        .forEach(blogFile -> blogFileCollectionDTO.getBlogFiles().add(this.fileMapper.domain2dto(blogFile)));
    return blogFileCollectionDTO;
  }

  @DeleteMapping(value = "/files/{id}")
  public boolean deleteFile(@PathVariable Long id) {
    return this.fileService.deleteFile(id);
  }

  @GetMapping("/files/page/{pageNumber}")
  public BlogFilePageDTO getBlogFilePage(@PathVariable Long pageNumber){
    return this.fileService.getBlogFilesPage(pageNumber.intValue());
  }

}
