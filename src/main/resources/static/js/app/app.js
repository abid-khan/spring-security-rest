var restApp = angular.module('restApp', [ 'ngRoute', 'ngSanitize']);
restApp
		.config([
				'$routeProvider',
				'$locationProvider',
				'$httpProvider',
				function($routeProvider, $locationProvider, $httpProvider) {

					$routeProvider.when('/', {
						templateUrl : 'html/home.html',
						controller : 'home',
						title:'Home'
					}).when('/login', {
						templateUrl : 'html/login.html',
						controller : 'loginController',
						title:'Login'
					}).when('/user', {
						templateUrl : 'html/user.html',
						controller : 'userController',
						title:'User'
					}).otherwise({
						redirectTo: '/'
					});
					

					//delete $httpProvider.defaults.headers.common['X-AuthToken'];

				} ]).controller("home", function($scope, $location, $http) {
					$scope.name = 'abid';
				});

restApp.run(function ($rootScope) {
    $rootScope.$on("$routeChangeSuccess", function (event, currentRoute, previousRoute) {
        document.title = currentRoute.title;
    });
});

restApp.controller("navigationController", function($rootScope, $scope, $http,
		$location, $route) {
	
	
	$scope.logout = function() {
		$http.post('http://localhost:8080/logout', {}).success(function() {
			$rootScope.authenticated = false;
			$location.path("/");
		}).error(function() {
			$rootScope.authenticated = false;
		});
	};

});

restApp.controller("userController", function($rootScope, $scope, $http,
		$location, $route) {
	
	$http.get('http://localhost:8080/api/users', {
		headers : {'X-AuthToken':$rootScope.authToken}
	})
	.success(function(data, status, headers, config){
		console.log("Fetched users successfully");
		$scope.users=data;
	})
	.error(function(data, status, headers, config){
		console.log("Failed to load users");
		$rootScope.authenticated = false;
	});

});



restApp.controller("loginController", function($rootScope, $scope, $http,
		$location, $route) {
	
	$scope.login = function() {
		$http.post(
				'http://localhost:8080/login',
				'username=' + $scope.credentials.username + '&password='
						+ $scope.credentials.password, {
					headers : {
						'Content-Type' : 'application/x-www-form-urlencoded'
					}
				}).success(function(data, status, headers, config) {
			// Success handler
			console.log("log in success");
			$rootScope.authenticated = true;
			$rootScope.authToken = headers('X-AuthToken');
			$location.path("/");

		}).error(function(data, status, headers, config) {
			// Failure handler
			console.log("log in faild");
			$rootScope.authenticated = false;
			$rootScope.authToken = null;
			$location.path("/login");
		});
	};

	

});
