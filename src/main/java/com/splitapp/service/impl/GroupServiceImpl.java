package com.splitapp.service.impl;

import com.splitapp.model.Group;
import com.splitapp.model.Member;
import com.splitapp.repository.GroupRepository;
import com.splitapp.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository; // DIP: inject interface via constructor

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @Override
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    public Group save(String name, List<String> memberNames) {
        Group group = new Group(name);
        for (String memberName : memberNames) {
            String trimmed = memberName.trim();
            if (!trimmed.isEmpty()) {
                group.getMembers().add(new Member(trimmed, group));
            }
        }
        return groupRepository.save(group);
    }

    @Override
    public void deleteById(Long id) {
        groupRepository.deleteById(id);
    }
}
