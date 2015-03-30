package security.schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import security.service.base.AuthTokenService;

@Service
public class CleanupAuthenticationTokenScheduler {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AuthTokenService authTokenService;

	//@Scheduled(cron = "${auth.cron.session.timeout}")
	public void cleanupTimedoutToken() {
		logger.info("Cleanup of expired authentication token begins");
		authTokenService.deleteExpiredTokens();
		logger.info("Cleanup of expired authentication token completes");
	}
}
