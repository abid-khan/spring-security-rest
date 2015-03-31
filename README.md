# Securing REST APIs using Spring Security#

# Table of Content

- [**Abstract**](#abstract)
- [**Introduction**](#introduction)
- [**Prerequisites**](#prerequisites)
- [**Solution**](#solution)
  - [Authorization](#authorization)
    - [Security Configuration](#security-configuration)
    - [Authorization Token Generation](#authorization-token-generation)
  - [Authentication](#authentication)
    - [Security Configuration](#security-configuration)
    - [Authentication Filter](#authentication-filter)
- [**How to Use**](#how-to-use)


## Abstract##
The objective of this project/example is showcase how [Spring Security](http://projects.spring.io/spring-security/) can be used to secure REST APIs. This project is based on [Spring Boot](http://projects.spring.io/spring-boot/) and mven is used for build purpose.

## Introduction ##
Before we actuallys take a deep dive, we should first dicuss how session is managed in an enterprise applicaiton. 
A user session is initiated and an unique id is generated once user  successfully logs into the system. The same session id is send to the server in next subsequent requests by the client. The user session is destroyed once user logs out or it is idle for more than specified duration. This whole process is statefull.

As REST advocates stateless session mechanism, to have above mentioned behaviour we have  classified URLs in two different categories. 
 * One set starts with root context i.e. `/`. This set of URLs use statefull session.
 * Second set starts with `/api`. This set of URLs use stateless session.

Also, to we have a scheduler configured which deletes expired or idle authorization tokens from the system. The frequency of the scheduler is configurable and discussed in detail in `How to Use` section.

 

## Prerequisites ##
One must have knowledge on bellow mentioned tools and technologies.
  * [Spring Boot](http://projects.spring.io/spring-boot/)
  * [Spring Security](http://projects.spring.io/spring-security/)
  * [Maven](http://maven.apache.org/)
  * [AngularJS](https://angularjs.org/)

## Solution ##
To achieve session management goal, we have differentiate whole process into three distinct process 
  * Authentication
  * Authorization
  * Clean up of expired/idle tokens



  
### Authentication ###
This process uses statefull session creation mechanism to authenticate the user and to generate authorization token. On successfull login, a authentication token is generated and added in response header. In this example/project it is reffered as `X-AuthToken`. This step uses statefull session creation policy. This step is typically consists of bellow sequence

  * User opens home page and clicks in log in page
  * User provides user name and password and clicks submit button
  * User is authenticated  and on success a authentication token is generated and added to reponse header. User forwarded to      desired page
  * On authentication failure, user is forwarded to login page with error
 

#### Security Configuration ####
First [Spring Security](http://projects.spring.io/spring-security/) is configured for first set of URLs which are in scope of stateful session. URLs like `/` and `/home`  are allowed to be accessed without authorization. Login page is configured by `/login`.  Apart from these, there are `authenticationSuccessHandler` and `authenticationFailureHandler`.

 * authenticationSuccessHandler is used to allow to place some cusotm code for post login success.
 * authenticationFailureHandler is used to allow to place some cusotm code for post login failure.

Also user detail service is  configured to validate user in system. For more detail on Spring Security configuration please refer [Spring Security](http://projects.spring.io/spring-security/).

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
Authorization token is generated in login success handler.  The token is  generation logic is placed in `AuthTokenGeneratorServiceImpl` class.

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

Note: Above configuration is marked with @Order(1). This is done to ensure that this configuration is executed before REST API seccurity configuration.

### Authorization ###
In this step RESTful resources are authorized against a valid authorization token. This process is mainly achieved in "TokenBasedAuthenticationFilter"  filter.  This step is typically consists of bellow sequence

 * Token is fetched from request header
 * Then lookup is done to check if user exists in system for current token
 * If present a instance of org.springframework.security.authentication.UsernamePasswordAuthenticationToken is returned.
 * If user is not present, null token is returned.
 

Most important is the security configuration for RESTful resources. Note that RESTful resources starts with `/api`. also this configured with order 2.

#### Security Configuration ####

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
```

#### Authentication Filter ####

```java
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
		request.setAttribute(TOKEN_FILTER_APPLIED, Boolean.TRUE);

		String token = request.getHeader(Constant.HEADER_SECURITY_TOKEN);
		userAuthenticationToken = authenticateByToken(token);
		if (userAuthenticationToken == null){
			throw new AuthenticationServiceException("Bad Token");
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
Integration of this project/example is very simple. As mentioned earlier this project is based on [Spring Boot](http://projects.spring.io/spring-boot/) and structured in such a way that one has to update some  configurations in application.proprties file located in classpath (i.e. /main/resources/).

```properties
# DataSource settings: set here configurations for the database connection
spring.datasource.url = jdbc:mysql://localhost:3306/[database name]
spring.datasource.username = [database user name]
spring.datasource.password = [database password]
spring.datasource.driverClassName = com.mysql.jdbc.Driver

# Specify the DBMS
spring.jpa.database = MYSQL

# Show or not log for each sql query
spring.jpa.show-sql = true

# Hibernate settings are prefixed with spring.jpa.hibernate.*
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect
spring.jpa.hibernate.naming_strategy = org.hibernate.cfg.ImprovedNamingStrategy


#Security Configuration
#Controller URL use to write post login success logic
auth.success.url=/hello
#auth.success.url=/auth/success
#Controller URL use to write post login failure logic
auth.failure.url=/auth/failure

#Cron expression to delete expired or idle session. Here it executes every 30 minutes
auth.cron.session.timeout=0 30 * * * *
#Session timeout duration
auth.token.timeout.interval=30
```

To run the application execute below command from command prompt.
```mvn
mvn spring-boot:run

```
To debug the same,execute below command.
```mvn
mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8080"

```
