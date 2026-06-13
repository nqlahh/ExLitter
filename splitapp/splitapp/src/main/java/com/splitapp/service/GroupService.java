package com.splitapp.service;

import com.splitapp.model.Group;
import java.util.List;
import java.util.Optional;

public interface GroupService {
    List<Group> findAll();
    Optional<Group> findById(Long id);
    Group save(String name, List<String> memberNames);
    void deleteById(Long id);
}
