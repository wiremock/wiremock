angular.module('admin')

    .factory('alertDialog', function($rootScope, $modal) {

        return {

            show: function(msg, type) {

                if (!type) {
                    type = 'successful';
                }

                var scope = $rootScope.$new(true);
                scope.msg = msg;
                scope.type = type;

                var modal = $modal.open({
                    animation: true,
                    templateUrl: 'partials/alert.html',
                    scope: scope
                });

                scope.closeAlert = function() {
                    modal.close();
                };
            }
        };
    });