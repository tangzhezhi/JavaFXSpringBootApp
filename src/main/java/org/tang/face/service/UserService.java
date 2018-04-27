package org.tang.face.service;

import org.tang.face.bean.User;
import org.tang.face.generic.GenericService;

public interface UserService extends GenericService<User> {

	boolean authenticate(String email, String password);
	
	User findByUserName(String email);
	
}
