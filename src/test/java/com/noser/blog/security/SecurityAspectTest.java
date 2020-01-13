package com.noser.blog.security;

import com.noser.blog.config.BlogProperties;
import com.noser.blog.domain.BlogFile;
import com.noser.blog.repository.ArticleRepository;
import com.noser.blog.repository.FileRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SecurityAspectTest {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    BlogProperties blogProperties;

    SecurityAspect objectUnderTest;

    @Before
    public void setup() {
        objectUnderTest = new SecurityAspect(this.articleRepository, this.blogProperties, this.fileRepository);

    }

    @Test
    @WithMockUser(username = "TEST USER", roles = {"user"})
    public void checkDeleteFileUser() throws UnauthorizedException {
        objectUnderTest.checkDeleteFile(new JoinPoint() {
            @Override
            public String toString() {
                return null;
            }

            @Override
            public String toShortString() {
                return null;
            }

            @Override
            public String toLongString() {
                return null;
            }

            @Override
            public Object getThis() {
                return null;
            }

            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Object[] getArgs() {
                return new Object[] {Long.valueOf(1l)};
            }

            @Override
            public Signature getSignature() {
                return null;
            }

            @Override
            public SourceLocation getSourceLocation() {
                return null;
            }

            @Override
            public String getKind() {
                return null;
            }

            @Override
            public StaticPart getStaticPart() {
                return null;
            }
        },null);
    }

    @Test
    @WithMockUser(username = "TEST USER", roles = {"admin"})
    public void checkDeleteFileAdmin() throws UnauthorizedException {
        objectUnderTest.checkDeleteFile(new JoinPoint() {
            @Override
            public String toShortString() {
                return null;
            }

            @Override
            public String toLongString() {
                return null;
            }

            @Override
            public Object getThis() {
                return null;
            }

            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Object[] getArgs() {
                return new Object[] {Long.valueOf(1l)};
            }

            @Override
            public Signature getSignature() {
                return null;
            }

            @Override
            public SourceLocation getSourceLocation() {
                return null;
            }

            @Override
            public String getKind() {
                return null;
            }

            @Override
            public StaticPart getStaticPart() {
                return null;
            }
        }, null);
    }
}
