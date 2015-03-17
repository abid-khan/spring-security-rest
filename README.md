# Securing RESTful APIs using Spring Security#

## Introduction##
The primary goal of this project/example is showcase how [Spring Security](http://projects.spring.io/spring-security/) can be used to secure RESTful APIs. This project is based on [Spring Boot](http://projects.spring.io/spring-boot/) and mven is used for build purpose. In this project, there are two different steps to achive the goal.
  * Authentication 
  * Authorization

## Prerequisites ##
One must have knowledge on bellow mentioned tools and technologies.
  * [Spring Boot](http://projects.spring.io/spring-boot/)
  * [Spring Security](http://projects.spring.io/spring-security/)
  * [Maven](http://maven.apache.org/)
  * [Thymeleaf](http://www.thymeleaf.org/)
  
### Authentication ###
In this phase is used to authenticate the user. User has to enter valid user name and password to login successfully. On successfull login , a authentication token is generated and added in response header. In this example/project it is reffered as X-AuthToken. This step uses statefull session creation policy. This step is tipycally consists of bellow sequnces

  * User opens home page and clicks in log in page
  * User provides user name and password and clicks submit button
  * User is authenticated  and on success a authentication token is generated and added to reponse header. User forwarded to      desired page
  * On authentication failure, user is forwarded to login page with error



#### Flow Diagram ####

### Authorization ###







## How to Use ##

## Conclusion##
