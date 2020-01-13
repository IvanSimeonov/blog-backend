package com.noser.blog.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.noser.blog.security.CheckDeleteFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.noser.blog.domain.BlogFile;
import com.noser.blog.domain.BlogFileView;
import com.noser.blog.domain.Thumbnail;
import com.noser.blog.repository.FileRepository;
import com.noser.blog.repository.ThumbnailRepository;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@Service
public class DBFileService implements FileService {

	private final FileRepository fileRepository;
	private final ThumbnailRepository thumbnailRepository;

	public DBFileService(final FileRepository fileRepository, final ThumbnailRepository thumbnailRepository) {
		this.fileRepository = fileRepository;
		this.thumbnailRepository = thumbnailRepository;
	}

	@Override
	public Long saveFile(BlogFile file) {
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

	@Override
	//@CheckGetAllFilesPermission
	public Iterable<BlogFileView> getFiles(String name) {
		return fileRepository.findBlobFileView();
	}

	@Override
	@CheckDeleteFile
	public boolean deleteFile(Long id) {

		if (!fileRepository.existsById(id)) {
			return false;
		}

		fileRepository.deleteById(id);
		return true;
	}

	@Override
	@Cacheable("thumbnails")
	public Thumbnail getThumbnail(BlogFile file, int size) {
		if (file == null || file.getData() == null || file.getId() == null || size < 10 || size > 10000) {
			return null;
		}
		
		String thumbnailId = String.format("%d_%d", file.getId(), size);
		
		if (this.thumbnailRepository.existsById(thumbnailId)) {
			return this.thumbnailRepository.findById(thumbnailId).get();
 		}
 		
		try {
			return createThumbnail(thumbnailId, file.getId(), file.getData(), size);
		} catch (IOException exception) {
			log.error("Cannot create thumbnail from file {}", file.getName());
			log.error(exception.getMessage());
		}
		
		return null;
	}
	
	private Thumbnail createThumbnail(final String id, final Long imageId, byte[] imageData, int size) throws IOException {
		Thumbnail thumbnail = Thumbnail.builder().id(id)
				.data(resize(imageData, size))
				.imageId(imageId)
				.build();
		
		return thumbnailRepository.save(thumbnail);
	}
	
	private byte[] resize(final byte[] originalData, int size) throws IOException {
	    InputStream imageIn = new ByteArrayInputStream(originalData);
	    BufferedImage bufferedImage = Thumbnails.of(imageIn)
	    		.size(size, size)
	    		.outputFormat("png")
	    		.imageType(BufferedImage.TYPE_INT_ARGB)
	    		.asBufferedImage();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bufferedImage, "png", baos);
	    return baos.toByteArray();
	  }

}
