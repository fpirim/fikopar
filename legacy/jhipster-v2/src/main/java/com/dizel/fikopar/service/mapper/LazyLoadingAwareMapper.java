package com.dizel.fikopar.service.mapper;

import java.util.Collection;
import org.hibernate.Hibernate;

public interface LazyLoadingAwareMapper {
    default boolean isNotLazyLoaded(Collection<?> sourceCollection) {
        return Hibernate.isInitialized(sourceCollection);
    }
}
