angular.module('admin')
    .controller('mockListCtrl', ['$scope', 'AdminService', '$modal', 'alertDialog', function($scope, AdminService, $modal, alertDialog){

        $scope.loadAllMappings = function() {

            AdminService.getAll().success(function(resp) {
                $scope.mappings = resp.mappings;
            }).error(function(err) {
                alertDialog.show('Error: Cannot fetch mappings', 'danger');
            });
        }

        $scope.createNewMapping = function() {

            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'partials/editMock.html',
                controller: 'EditMockCtrl',
                size: 'lg',
                resolve: {
                    ExistingMock: function() {
                        return null;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.loadAllMappings();
            });
        }

        $scope.saveMapping = function() {

            AdminService.saveMapping()
                .success(
                    function() {
                        alertDialog.show('Mappings Saved Successfully');
                    }
                ).error(
                    function() {
                        alertDialog.show('Error: Cannot save mappings', 'danger');
                    }
                );
        }

        $scope.loadAllMappings();

    }]);