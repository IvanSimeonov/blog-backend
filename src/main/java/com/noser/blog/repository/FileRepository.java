package com.noser.blog.repository;

import com.noser.blog.domain.BlogFile;
import com.noser.blog.domain.BlogFileView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface FileRepository extends PagingAndSortingRepository<BlogFile,Long> {

	Page<BlogFile> findAll(Pageable pageable);
//	List<BlogFile> findAllByOrderByUploadedDesc();
//	List<BlogFile> findAllByOrderByAuthorId(String authorId);
//	List<BlogFile> findAllByOrderByNameDesc(String blogFileName);
	
	@Query("SELECT new com.noser.blog.domain.BlogFileView(b.id, b.name) FROM BlogFile b")
	Iterable<BlogFileView> findBlobFileView();
}
