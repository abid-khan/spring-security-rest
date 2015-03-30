package security.service.base;

import security.entity.AuthToken;

/**
 * @author abidk
 * 
 */
public interface AuthTokenService {

	AuthToken create(AuthToken authToken);

	AuthToken update(AuthToken authToken);

	AuthToken findUserByTokenAndSeries(final String token, final String series);

	void deleteByTokenAndSeries(final String token, final String series);

	void deleteExpiredTokens();
}
