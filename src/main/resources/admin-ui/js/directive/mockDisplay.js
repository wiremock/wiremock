angular.module('admin')

    .directive('mockDisplay', function() {
        return {
            restrict: 'A',
            templateUrl: 'partials/mockDisplay.html',
            scope: {
                mock: '='
            },
            controller: function ($scope, $http, $modal) {

                $scope.formatBody = function(data) {

                    if(typeof data === 'object') {
                        return JSON.stringify(data, undefined, 4);
                    } else {
                        return data;
                    }
                }

                $scope.init = function() {

                    if ($scope.mock.response.bodyFileName) {

                        var filePath = $scope.mock.response.bodyFileName;

                        $http({
                            url: '/' + filePath,
                            method: 'GET',
                            transformResponse: [function (data) {
                                return $scope.formatBody(data);
                            }]
                        }).success(
                            function(data) {
                                $scope.bodyFileData =data;
                            }
                        ).error(
                            function(error) {
                                console.log(error);
                            }
                        );
                    } else if($scope.mock.response.body) {
                        $scope.bodyData = $scope.formatBody($scope.mock.response.body);
                    }
                };

                $scope.init();

                $scope.editMock = function() {
                    var modalInstance = $modal.open({
                        animation: true,
                        templateUrl: 'partials/editMock.html',
                        controller: 'EditMockCtrl',
                        size: 'lg',
                        resolve: {
                            ExistingMock: function() {
                                return $scope.mock;
                            }
                        }
                    });

                    modalInstance.result.then(function (mock) {
                        angular.extend($scope.mock, mock);
                        $scope.init();
                    });
                };

                $scope.getObjectKeysLength = function(obj) {
                    if (obj) {
                        return Object.keys(obj).length;
                    }
                    return 0;
                }
            }
        };
    });