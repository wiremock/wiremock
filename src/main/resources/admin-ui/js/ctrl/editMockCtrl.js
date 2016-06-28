angular.module('admin')

    .controller('EditMockCtrl', ['$scope', '$modalInstance', 'AdminService', 'ExistingMock', 'alertDialog', '$http',
        function($scope, $modalInstance, AdminService, ExistingMock, alertDialog, $http) {

            $scope.formatBody = function(data) {

                if(typeof data === 'object') {
                    return JSON.stringify(data, undefined, 4);
                } else {
                    return data;
                }
            };

            $scope.init = function() {

                $scope.predicates = [ 'equalTo', 'contains', 'matches', 'doesNotMatch', 'absent',
                    'matchesJsonPath', 'equalToJson', 'equalToXml', 'matchesXPath', 'xpathNamespaces'];

                $scope.mock = {
                    request: {},
                    response: {}
                };

                $scope.tempData = {
                    urlType: 'urlPattern',
                    bodyFileDataChanged: false
                };

                $scope.editMode = !!ExistingMock;

                $scope.title = $scope.editMode ? 'Edit Mapping':'Add New Mapping';

                if($scope.editMode) {

                    $scope.mock = angular.copy(ExistingMock);

                    for (var key in $scope.mock.request) {
                        if(key.indexOf('url') == 0) {
                            $scope.tempData.urlType = key;
                        }
                    }

                    if ($scope.mock.response.bodyFileName) {

                        $http({
                            url: '/' + $scope.mock.response.bodyFileName,
                            method: 'GET',
                            transformResponse: [function (data) {
                                return $scope.formatBody(data);
                            }]
                        }).success(
                            function(data) {
                                $scope.tempData.bodyFileData =data;
                            }
                        ).error(
                            function(error) {
                                console.log(error);
                            }
                        );
                    }
                }
            };

            $scope.init();

            $scope.removeQueryParameter = function(queryParam) {
                delete $scope.mock.request.queryParameters[queryParam];
            };

            $scope.addQueryParameter = function() {

                if (!$scope.mock.request.queryParameters) {
                    $scope.mock.request.queryParameters = {};
                }

                var valuePattern = {};
                valuePattern[$scope.tempData.addQueryPredicate] = $scope.tempData.addQueryPattern;

                $scope.mock.request.queryParameters[$scope.tempData.addQueryParam] = valuePattern;

                $scope.tempData.addQueryParam = null;
                $scope.tempData.addQueryPredicate = null;
                $scope.tempData.addQueryPattern = null;
            }

            $scope.removeHeaderParameter = function(headerParam) {
                delete $scope.mock.request.headers[headerParam];
            };

            $scope.addHeaderParameter = function() {

                if (!$scope.mock.request.headers) {
                    $scope.mock.request.headers = {};
                }

                var valuePattern = {};
                valuePattern[$scope.tempData.addHeaderPredicate] = $scope.tempData.addHeaderPattern;

                $scope.mock.request.headers[$scope.tempData.addHeaderParam] = valuePattern;

                $scope.tempData.addHeaderParam = null;
                $scope.tempData.addHeaderPredicate = null;
                $scope.tempData.addHeaderPattern = null;
            }

            $scope.removeBodyParameter = function(bodyParam) {

                var index = $scope.mock.request.bodyPatterns.indexOf(bodyParam);
                $scope.mock.request.bodyPatterns.splice(index, 1);
            };

            $scope.addBodyParameter = function() {

                if (!$scope.mock.request.bodyPatterns) {
                    $scope.mock.request.bodyPatterns = [];
                }

                var valuePattern = {};
                valuePattern[$scope.tempData.addBodyPredicate] = $scope.tempData.addBodyPattern;

                $scope.mock.request.bodyPatterns.push(valuePattern);

                $scope.tempData.addBodyPredicate = null;
                $scope.tempData.addBodyPattern = null;
            }

            $scope.removeResponseHeader = function(header) {
                delete $scope.mock.response.headers[header];
            };

            $scope.addResponseHeader = function() {

                if (!$scope.mock.response.headers) {
                    $scope.mock.response.headers = {};
                }

                $scope.mock.response.headers[$scope.tempData.addRespHeader] = $scope.tempData.addRespHeaderValue;

                $scope.tempData.addRespHeader = null;
                $scope.tempData.addRespHeaderValue = null;
            }

            $scope.save = function () {

                var mapping = $scope.mock;

                for (var key in mapping.request) {
                    if(key.indexOf('url') == 0 && key != $scope.tempData.urlType) {
                        delete mapping.request[key];
                    }
                }

                if ($scope.editMode) {

                    if ($scope.tempData.bodyFileDataChanged) {
                        mapping.response.body = $scope.tempData.bodyFileData;
                        delete mapping.response.bodyFileName;
                    }

                    AdminService.editMapping(mapping)
                        .success(
                        function(data) {
                            $modalInstance.close(mapping);
                            alertDialog.show('Mapping edited Successfully. Please note that all edited mappings are transient until saved to disk',
                                'successful');
                        }
                    ).error(
                        function(error) {
                            $modalInstance.dismiss('error');
                            alertDialog.show('Error: Cannot edit mapping', 'danger');
                        }
                    );

                } else {

                    AdminService.addMapping(mapping)
                        .success(
                        function(data) {
                            $modalInstance.close();
                            alertDialog.show('Mapping added Successfully. Please note that all added mappings are transient until saved to disk',
                                'successful');
                        }
                    ).error(
                        function(error) {
                            $modalInstance.dismiss('error');
                            alertDialog.show('Error: Cannot Add mapping', 'danger');
                        }
                    );
                }
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };

            $scope.getFirstPropertyKey = function(obj) {
                var keys = Object.keys(obj);
                if (keys && keys.length > 0 ) {
                    return keys[0];
                }
                return null;
            }
        }]);