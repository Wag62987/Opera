package com.Innocent.DevOpsAsistant.Devops.Assistant.Interfaces;

import java.util.Optional;

public interface CrudService <T,id> {

     T Save(T t);
     Optional<T> FindById(String id);

}
