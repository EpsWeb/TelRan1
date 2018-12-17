package telran.ashkelon2018.forum.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.ashkelon2018.forum.configuration.AccountConfiguration;
import telran.ashkelon2018.forum.configuration.AccountUserCredential;
import telran.ashkelon2018.forum.dao.UserAccountRepository;
import telran.ashkelon2018.forum.domain.UserAccount;
import telran.ashkelon2018.forum.dto.UserProfileDto;
import telran.ashkelon2018.forum.dto.UserRegDto;
import telran.ashkelon2018.forum.exceptions.UserConflictException;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	UserAccountRepository userAccountRepository;

	@Autowired
	AccountConfiguration accountConfiguration;

	@Override
	public UserProfileDto addUser(UserRegDto userRegDto, String token) {
		AccountUserCredential credential = accountConfiguration.tokenDecode(token);
		if (userAccountRepository.existsById(credential.getLogin())) {
			throw new UserConflictException();
		}
		String hashPassword = BCrypt.hashpw(credential.getPassword(), BCrypt.gensalt());
		UserAccount userAccount = UserAccount.builder().login(credential.getLogin()).password(hashPassword)
				.firstName(userRegDto.getFirstName()).lastName(userRegDto.getLastName()).role("User")
				.expdate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod())).build();

		userAccountRepository.save(userAccount);

		return convertToUserProfileDto(userAccount);
	}

	private UserProfileDto convertToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder().firstName(userAccount.getFirstName()).lastName(userAccount.getLastName())
				.login(userAccount.getLogin()).roles(userAccount.getRoles()).build();
	}

	@Override
	public UserProfileDto editUser(UserRegDto userRegDto, String token) {

		AccountUserCredential credential = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userAccountRepository.findById(credential.getLogin()).get();
		if (userRegDto.getLastName() != null) {
			userAccount.setLastName(userRegDto.getLastName());
		}
		userAccountRepository.save(userAccount);
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto removeUser(String login, String token) {
		// FIXME
		UserAccount userAccount = userAccountRepository.findById(login).orElse(null);
		if (userAccount != null) {
			userAccountRepository.delete(userAccount);
		}
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public Set<String> addRole(String login, String role, String token) {

		// FIXME
		UserAccount userAccount = userAccountRepository.findById(login).orElse(null);
		if (userAccount != null) {
			userAccount.addRole(role);
			userAccountRepository.save(userAccount);
		} else {
			return null;
		}

		return userAccount.getRoles();
	}

	@Override
	public Set<String> removeRole(String login, String role, String token) {
		UserAccount userAccount = userAccountRepository.findById(login).orElse(null);
		if (userAccount != null) {
			userAccount.removeRole(role);
			userAccountRepository.save(userAccount);
		} else {
			return null;
		}

		return userAccount.getRoles();
	}

	@Override
	public void changePassword(String password, String token) {

		AccountUserCredential credential = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userAccountRepository.findById(credential.getLogin()).get();
		String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		userAccount.setPassword(hashPassword);
		userAccount.setExpdate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod()));
		userAccountRepository.save(userAccount);
	}

	@Override
	public UserProfileDto login(String token) {

		AccountUserCredential credential = accountConfiguration.tokenDecode(token);
		UserAccount userAccount = userAccountRepository.findById(credential.getLogin()).get();

		return convertToUserProfileDto(userAccount);
	}

}
