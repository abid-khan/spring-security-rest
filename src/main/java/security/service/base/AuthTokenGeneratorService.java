package security.service.base;

import org.springframework.security.core.Authentication;

/**
 * @author abidk
 *
 */
public interface AuthTokenGeneratorService {

	String generateToken(Authentication authentication);

	public String[] decode(String token);
}
