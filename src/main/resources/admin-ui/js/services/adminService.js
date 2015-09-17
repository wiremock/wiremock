angular.module('admin')

    // super simple service
    // each function returns a promise object
    .factory('AdminService', ['AdminApiServiceWrapper', function (AdminApiServiceWrapper) {

        return {

            getAll: function () {
                return AdminApiServiceWrapper.get('/');
            },

            addMapping: function(mapping) {
                return AdminApiServiceWrapper.post('/mappings/new', mapping);
            },

            saveMapping: function() {
                return AdminApiServiceWrapper.post('/mappings/save');
            },

            editMapping: function(mapping) {
                return AdminApiServiceWrapper.post('/mappings/edit', mapping);
            },

            getRequestCount: function(request) {
                return AdminApiServiceWrapper.post('/requests/count', request);
            }
        };
    }]);