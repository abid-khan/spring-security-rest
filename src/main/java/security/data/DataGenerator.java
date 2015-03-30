package security.data;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import security.entity.User;
import security.repository.UserRepository;

/**
 * @author abidk
 *
 */
@Service
public class DataGenerator {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	public void initialize() {

		User user = userRepository.findByEmail("abid");
		if (null == user) {
			createBootUser();
		}
	}

	@Transactional
	public void createBootUser() {

		User user = new User();
		user.setEmail("abid");
		user.setPassword(bCryptPasswordEncoder.encode("abid"));
		userRepository.saveAndFlush(user);

	}
}
