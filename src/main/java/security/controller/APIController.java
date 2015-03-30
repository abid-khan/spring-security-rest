package security.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import security.entity.User;
import security.service.base.UserService;

@RestController()
public class APIController {

	@Autowired
	UserService userService;

	@RequestMapping(value = "/api/users", method = RequestMethod.GET, produces = "application/json")
	public List<User> getUsers() {
		return userService.findAll();
	}
}
