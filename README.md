# Securing RESTful APIs using Spring Security#

## Introduction##
The primary goal of this project/example is showcase how [Spring Security](http://projects.spring.io/spring-security/) can be used to secure RESTful APIs. This project is based on [Spring Boot](http://projects.spring.io/spring-boot/) and mven is used for build purpose.

Also I have used two different sets of URL in this project
 * One set starts with root context i.e. "/". This set of URLs use statefull session.
 * Second set starts with "/api". This set of URLs use stateless session.
 * 

In this project, there are two different steps to achive the goal.
  * Authentication 
  * Authorization


## Prerequisites ##
One must have knowledge on bellow mentioned tools and technologies.
  * [Spring Boot](http://projects.spring.io/spring-boot/)
  * [Spring Security](http://projects.spring.io/spring-security/)
  * [Maven](http://maven.apache.org/)
  * [Thymeleaf](http://www.thymeleaf.org/)
  
### Authentication ###
In this phase is used to authenticate the user. User has to enter valid user name and password to login successfully. On successfull login , a authentication token is generated and added in response header. In this example/project it is reffered as X-AuthToken. This step uses statefull session creation policy. This step is tipycally consists of bellow sequence

  * User opens home page and clicks in log in page
  * User provides user name and password and clicks submit button
  * User is authenticated  and on success a authentication token is generated and added to reponse header. User forwarded to      desired page
  * On authentication failure, user is forwarded to login page with error
 
#### MVC Configuration ####
In this configuration , URLs are mapped to view.

```java
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/home").setViewName("home");
		registry.addViewController("/").setViewName("home");
		registry.addViewController("/hello").setViewName("hello");
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/api/home").setViewName("api");
	}

}
```

#### Security Configuration ####
First [Spring Security](http://projects.spring.io/spring-security/) is configured for first set of URLs which are in scope of stateful session. URLs like "/" and "/home"  are allowed to be accessed without authorization. Login page is ocnfigured by "/login".  Apart from these, there are authenticationSuccessHandler and authenticationFailureHandler.

 * authenticationSuccessHandler is used to allow to place some cusotm code for post login success.
 * authenticationFailureHandler is used to allow to place some cusotm code for post login failure.

Also user detail service is  configured to validate user in system.

```java
@Configuration
@EnableWebMvcSecurity
@Order(1)
public class MvcSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/", "/home")
				.permitAll().anyRequest().authenticated().and().formLogin()
				.failureHandler(authenticationFailureHandler())
				.successHandler(authenticationSuccessHandler())
				.loginPage("/login").permitAll().and().logout().permitAll();

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(
				bCryptPasswordEncoder());
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new AuthenticationSuccessHandlerImpl();
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new AuthenticationFailureHandlerImpl();
	}

}
```
#### Authorization Token Generation ####
Authorization token is generated in login success handler.  The token is  generation logic is placed in "AuthTokenGeneratorServiceImpl" class.

```java
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
```

Note: Above configuration is marked with @Order(1). This is done to ensure that this configuration is executed before RESTful API seccurity configuration.

### Authorization ###
In this step RESTful resources are authorized for access. This authorization is done against a valid token. This process is mainly achieved in "TokenBasedAuthenticationFilter"  filter.  This step is tipycally consists of bellow sequence

 * Token is fetched from request header
 * Then lookup is done to check if user exists in system for current token
 * If present a instance of org.springframework.security.authentication.UsernamePasswordAuthenticationToken is returned.
 * If user is not present, null token is returned.
 

Most important is the security configuration for RESTful resources. Note that RESTful resources starts with "/api". also this configured with order 2.

```java
@Configuration
@EnableWebMvcSecurity
@Order(2)
public class APISecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthTokenGeneratorService authTokenGeneratorService;

	@Autowired
	private AuthTokenService authTokenService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/api/**")
				.csrf()
				.disable()
				.authorizeRequests()
				.anyRequest()
				.authenticated()
				.and()
				.addFilterBefore(tokenBasedAuthenticationFilter(),
						BasicAuthenticationFilter.class).sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling()
				.authenticationEntryPoint(new Http403ForbiddenEntryPoint());
	}

	@Bean
	public TokenBasedAuthenticationFilter tokenBasedAuthenticationFilter() {
		return new TokenBasedAuthenticationFilter("/api/**",
				authTokenGeneratorService, authTokenService);
	}
}

public class TokenBasedAuthenticationFilter extends
		AbstractAuthenticationProcessingFilter {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String TOKEN_FILTER_APPLIED = "TOKEN_FILTER_APPLIED";
	private AuthTokenGeneratorService authTokenGeneratorService;
	private AuthTokenService authTokenService;

	public TokenBasedAuthenticationFilter(String defaultFilterProcessesUrl,
			AuthTokenGeneratorService authTokenGeneratorService,
			AuthTokenService authTokenService) {
		super(defaultFilterProcessesUrl);
		super.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(
				defaultFilterProcessesUrl));
		super.setAuthenticationManager(new NoOpAuthenticationManager());
		setAuthenticationSuccessHandler(new TokenBasedAuthenticationSuccessHandlerImpl());
		this.authTokenGeneratorService = authTokenGeneratorService;
		this.authTokenService = authTokenService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse arg1) throws AuthenticationException,
			IOException, ServletException {

		AbstractAuthenticationToken userAuthenticationToken = null;
		try {
			request.setAttribute(TOKEN_FILTER_APPLIED, Boolean.TRUE);

			String token = request.getHeader(Constant.HEADER_SECURITY_TOKEN);
			userAuthenticationToken = authenticateByToken(token);
			if (userAuthenticationToken == null)
				throw new AuthenticationServiceException(MessageFormat.format(
						"Error | {0}", "Bad Token"));
		} catch (Exception ex) {
			logger.error("Failed to authenticate user  due to { }", ex);
		}

		return userAuthenticationToken;
	}

	/**
	 * authenticate the user based on token
	 * 
	 * @return
	 */
	private AbstractAuthenticationToken authenticateByToken(String token) {
		if (null == token) {
			return null;
		}

		AbstractAuthenticationToken authToken = null;

		try {
			String[] tokens = authTokenGeneratorService.decode(token);

			User user = authTokenService.findUserByTokenAndSeries(tokens[0],
					tokens[1]);
			if (null == user) {
				return null;
			}

			security.bean.User securityUser = new security.bean.User(user);

			authToken = new UsernamePasswordAuthenticationToken(
					securityUser.getUsername(), "",
					securityUser.getAuthorities());
		} catch (Exception ex) {
			logger.error("Failed to authenticate user for token" + token
					+ "{ }", ex);
		}

		return authToken;
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;

		if (request.getAttribute(TOKEN_FILTER_APPLIED) != null) {
			arg2.doFilter(request, response);
		} else {
			super.doFilter(arg0, arg1, arg2);
		}

	}

}

```







## How to Use ##

## Conclusion##
