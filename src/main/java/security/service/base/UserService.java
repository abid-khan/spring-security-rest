package security.service.base;

import java.util.List;

import security.entity.User;

/**
 * @author abidk
 * 
 */
public interface UserService {

	List<User> findAll();
}
