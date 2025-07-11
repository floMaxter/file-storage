package com.projects.filestorage.domain;

import java.io.Serializable;

public interface BaseEntity<T extends Serializable> extends Serializable {

    T getId();

    void setId(T id);
}
