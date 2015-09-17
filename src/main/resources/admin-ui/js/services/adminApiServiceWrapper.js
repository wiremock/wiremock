angular.module('admin')


    .factory('AdminApiServiceWrapper', ['$http', '$q', function($http, $q){

        var adminApiRequest = function (config) {

            var request = {
                url:     '/__admin' + config.path,
                method:  config.method,
                params: config.params,
                headers: {}
            };

            if (config.data) {
                request.headers['Content-Type'] = 'application/json';
                request.data = config.data;
            }

            return $http(request);
        };

        return {

            get: function (path, params) {

                var config = {
                    method: 'GET',
                    params: params,
                    path: path
                };

                return adminApiRequest(config);
            },

            post: function (path, data, params ) {

                var config = {
                    method: 'POST',
                    path: path,
                    params: params,
                    data: data
                };

                return adminApiRequest(config);
            },

            put: function (path, data, params) {

                var config = {
                    method: 'PUT',
                    path: path,
                    params: params,
                    data: data
                };

                return adminApiRequest(config);
            },

            delete: function (path) {

                var config = {
                    method: 'DELETE',
                    path: path
                };

                return adminApiRequest(config);
            },

            patch: function (path, data, params) {

                var config = {
                    method: 'PATCH',
                    path: path,
                    params: params,
                    data: data
                };

                return adminApiRequest(config);
            }
        };
    }]);