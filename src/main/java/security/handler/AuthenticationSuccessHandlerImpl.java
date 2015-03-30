package security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import security.constant.Constant;
import security.service.base.AuthTokenGeneratorService;

/**
 * @author abidk
 * 
 */
public class AuthenticationSuccessHandlerImpl extends
		SimpleUrlAuthenticationSuccessHandler {

	@Value("${auth.success.url}")
	private String defaultTargetUrl;

	@Autowired
	private AuthTokenGeneratorService authTokenGeneratorService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		final String authToken = authTokenGeneratorService
				.generateToken(authentication);
		response.addHeader(Constant.HEADER_SECURITY_TOKEN, authToken);
		request.getRequestDispatcher(defaultTargetUrl).forward(request,
				response);

	}

}
