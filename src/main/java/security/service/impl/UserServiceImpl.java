package security.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import security.entity.User;
import security.repository.UserRepository;
import security.service.base.UserService;

@Transactional(readOnly=true)
@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Override
	public List<User> findAll(){
		return userRepository.findAll();
	}

}
