angular.module('admin', ['ngRoute', 'ui.bootstrap', 'ngSanitize', 'countTo'])

    .config(['$routeProvider', function ($routeProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'partials/overview.html',
                controller: 'overviewCtrl'
            })
            .when('/available-mocks', {
                templateUrl: 'partials/mockList.html',
                controller: 'mockListCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    }])

    .controller('appCtrl', ['$scope', '$rootScope', function($scope, $rootScope){


    }]);