package com.example.ratelimiter.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative opt-in for rate limiting on a controller method. {@code
 * RateLimitInterceptor} looks for this annotation on the matched handler and,
 * if present, enforces the resource's currently-configured algorithm before
 * letting the request reach the controller.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /** The resource id this endpoint is limited under; must be registered via the admin API first. */
    String resource();

    /** Request header carrying the caller's identity; falls back to the remote address when absent. */
    String keyHeader() default "X-Client-Id";
}
