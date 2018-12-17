package telran.ashkelon2018.forum.service;

import telran.ashkelon2018.forum.dto.UserProfileDto;
import telran.ashkelon2018.forum.dto.UserRegDto;

public interface UserService {

	UserProfileDto addUser(UserRegDto userRegDto, String token);
	
}
