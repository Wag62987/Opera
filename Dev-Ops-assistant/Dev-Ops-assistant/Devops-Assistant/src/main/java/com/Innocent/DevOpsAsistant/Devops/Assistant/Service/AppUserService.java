package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.UserDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Interfaces.CrudService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.AppUserRepository;

@Service
public class AppUserService implements CrudService<AppUser,Long> {
    @Autowired
    AppUserRepository userRepository;


    @Override
    public AppUser Save(AppUser appUser) {
        return userRepository.save(appUser);
    }

    @Override
    public Optional<AppUser> FindById(String githubid) {
        Optional<AppUser> existAppUser=  userRepository.findByGithubId(githubid);
        return existAppUser;
    }
    public Optional<AppUser> FindByEmail(String email) {
        Optional<AppUser> existAppUser=  userRepository.findByEmail(email);
        return existAppUser;
    }
    public UserDTO GetUserInfo(Optional<AppUser> appUser){
        if(appUser.isPresent()){
             AppUser user = appUser.get();
             UserDTO existingUser = new UserDTO();
             existingUser.setUsername(user.getUsername());
             existingUser.setName(user.getName());
             existingUser.setEmail(user.getEmail());
             return existingUser;
        }
        return null;
    }
}
