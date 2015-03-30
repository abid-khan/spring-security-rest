package security.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import security.entity.User;
import security.repository.UserRepository;

/**
 * @author abidk
 * 
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String arg0)
			throws UsernameNotFoundException {

		User user = userRepository.findByEmail(arg0);
		if (null == user) {
			// TODO
		}
		return new security.bean.User(user);
	}

}
