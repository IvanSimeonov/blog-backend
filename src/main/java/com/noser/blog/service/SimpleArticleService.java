package com.noser.blog.service;

import com.noser.blog.api.ArticleCollectionDTO;
import com.noser.blog.api.ArticleDTO;
import com.noser.blog.audit.CheckViewArticlePermission;
import com.noser.blog.audit.EnableLogging;
import com.noser.blog.domain.Article;
import com.noser.blog.mapper.ArticleMapper;
import com.noser.blog.repository.ArticleRepository;
import com.noser.blog.security.AccessRights;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SimpleArticleService implements ArticleService {

	private final ArticleRepository articleRepository;

	private final ArticleMapper articleMapper;

	public SimpleArticleService(ArticleRepository articleRepository, ArticleMapper articleMapper) {
		this.articleRepository = articleRepository;
		this.articleMapper = articleMapper;
	}

	@Override
	public ArticleCollectionDTO getAllArticles(final Principal principal, Authentication authentication) {

		Iterable<Article> allArticles = articleRepository.findAllByOrderByCreatedDesc();

		return ArticleCollectionDTO.builder()
			.published(
					StreamSupport.stream(allArticles.spliterator(), false).filter(article -> article.isPublished())
							.map(article -> articleMapper.domain2dto(article, principal, authentication))
							.collect(Collectors.toList()))
			.featured(StreamSupport.stream(allArticles.spliterator(), false).filter(article -> article.isFeatured())
					.map(article -> articleMapper.domain2dto(article, principal, authentication))
					.collect(Collectors.toList()))
			.own(StreamSupport.stream(allArticles.spliterator(), false)
					.filter(article -> article != null && principal != null
							&& article.getAuthorId().equals(principal.getName()))
					.map(article -> articleMapper.domain2dto(article, principal, authentication))
					.collect(Collectors.toList()))
			.unpublished(StreamSupport.stream(allArticles.spliterator(), false).filter(article -> {
				if (article.isPublished()) {
					return false;
				}

				if (principal == null || authentication == null) {
					return false;
				}

				if (principal.getName().equals(article.getAuthorId())) {
					return true;
				}

				if (authentication.getAuthorities().stream()
						.anyMatch(authority -> "publisher".equals(authority.getAuthority())
								|| "admin".equals(authority.getAuthority()))) {
					return true;
				}

				return false;
			}).map(article -> articleMapper.domain2dto(article, principal, authentication))
					.collect(Collectors.toList()))
			.build();
	}

	@Override
	@CheckViewArticlePermission
	public ArticleDTO getArticle(Long id, Principal principal, Authentication authentication) {
		Optional<Article> articleOptional = articleRepository.findById(id);
		if (articleOptional.isPresent()) {
			final Article article = articleOptional.get();

			if (AccessRights.canUserViewArticle(article, principal, authentication)) {
				return articleMapper.domain2dto(article, principal, authentication);
			}
		}
		return null;
	}

	@Override
	public ArticleDTO createArticle(Article article, Principal principal, Authentication authentication) {
		article.setAuthorId(principal.getName());
		article.setCreated(LocalDateTime.now());
		return articleMapper.domain2dto(articleRepository.save(article), principal, authentication);
	}

	@Override
	public ArticleDTO editArticle(Article article, Principal principal, Authentication authentication)
			throws IllegalAccessException {
		final Optional<Article> existingArticleOptional = articleRepository.findById(article.getId());
		if (existingArticleOptional.get() == null) {
			throw new IllegalAccessException("Unknown article");
		}

		final Article existingArticle = existingArticleOptional.get();
		existingArticle.setContent(article.getContent());
		existingArticle.setTitle(article.getTitle());
		existingArticle.setSubtitle(article.getSubtitle());
		existingArticle.setPublished(article.isPublished());
		existingArticle.setFeatured(article.isFeatured());
		existingArticle.setImageId(article.getImageId());
		ArticleDTO articleDTO = articleMapper.domain2dto(existingArticle, principal, authentication);
		if (false == articleDTO.isEditable()) {
			throw new IllegalAccessException("Unauthorized access");
		}
		this.articleRepository.save(existingArticle);
		return articleDTO;
	}

	@Override
	public boolean allowedToEditArticle(Long id, Principal principal, Authentication authentication) {
		Optional<Article> articleOptional = this.articleRepository.findById(id);

		if (!articleOptional.isPresent()) {
			return false;
		}

		return AccessRights.canUserEditArticle(articleOptional.get(), principal, authentication);
	}

	@Override
	@EnableLogging
	public boolean allowedToViewArticle(Long id, Principal principal, Authentication authentication) {
		Optional<Article> articleOptional = this.articleRepository.findById(id);
		if (articleOptional.isPresent()) {
			return AccessRights.canUserViewArticle(articleOptional.get(), principal, authentication);
		}
		return false;
	}

	@Override
	public boolean allowedToManageFiles(Principal principal, Authentication authentication) {
		if (principal == null || authentication == null) {
			return false;
		}

		return authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("user"));
	}
}
