package com.noser.blog.security;

import com.noser.blog.domain.Article;
import com.noser.blog.domain.BlogFile;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public class AccessRights {

  public static boolean canUserDeleteFile(final BlogFile blogFile, final  Principal principal, final Authentication authentication){
      if (authentication==null || authentication.getAuthorities()== null || authentication.getAuthorities().isEmpty()){
          return false;
      }

      if (isAdmin(authentication)) {
          return true;
      }

      return isOwnFile(blogFile,principal);

  }

  public static boolean canUserEditArticle(final Article article, final Principal principal, final Authentication authentication) {
	if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
      return false;
    }

    if (isOwnArticle(article, principal)) {
      return true;
    }

    return isAdminOrPublisher(authentication);
  }

  public static boolean canUserViewArticle(final Article article, final Principal principal, final Authentication authentication) {
    if (article == null) {
      return false;
    }

    if (article.isPublished()) {
      return true;
    }

    return isOwnArticle(article, principal) || canUserEditArticle(article, principal, authentication);
  }

  public static boolean isOwnArticle(Article article, Principal principal) {
    if (article == null || principal == null || article.getAuthorId() == null || principal.getName() == null) {
      return false;
    }

    return article.getAuthorId().equals(principal.getName());
  }

  public static boolean isOwnFile(BlogFile blogFile, Principal principal){
      if (blogFile == null || principal == null || blogFile.getAuthorId() == null || principal.getName() == null){
          return false;
      }
      return blogFile.getAuthorId().equals(principal.getName());
  }

  public static boolean isAdminOrPublisher(Authentication authentication) {
    if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(authority ->
            authority.getAuthority().equals("admin")
                || authority.getAuthority().equals("publisher"));
  }
  
  public static boolean isAdmin(Authentication authentication) {
	  if (authentication == null) {
		  return false;
	  }
	  
	  return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("admin"));
  }

  public static boolean canUserDeleteArticle(Article article, Principal principal, Authentication authentication) {
	  if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
	      return false;
	  } 
	  
	  return isOwnArticle(article, principal) || isAdmin(authentication);
  }
}
