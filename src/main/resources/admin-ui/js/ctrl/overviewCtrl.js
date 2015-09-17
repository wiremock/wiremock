angular.module('admin')
    .controller('overviewCtrl', ['$scope', 'AdminService', function($scope, AdminService){

        $scope.getAllRequestCount = function() {

            var req = {
                urlPattern: '.+',
                method: 'ANY'
            };

            AdminService.getRequestCount(req)
                .success(
                function(data) {
                    $scope.mockReqCount = data.count;
                }
            )
        }

        $scope.getAvailableMockCount = function() {

            var req = {
                urlPattern: '.+',
                method: 'ANY'
            };

            AdminService.getAll()
                .success(
                function(data) {
                    $scope.availableMockCount = data.mappings.length;
                }
            )
        }

        $scope.getAllRequestCount();
        $scope.getAvailableMockCount();

    }]);