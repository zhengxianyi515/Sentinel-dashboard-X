/**
 * Authority rule controller.
 */
angular.module('sentinelDashboardApp').controller('AuthorityRuleController', ['$scope', '$stateParams', 'AuthorityRuleService', 'ngDialog',
    'MachineService',
    function ($scope, $stateParams, AuthorityRuleService, ngDialog,
              MachineService) {
        $scope.app = $stateParams.app;

        $scope.rulesPageConfig = {
            pageSize: 10,
            currentPageIndex: 1,
            totalPage: 1,
            totalCount: 0,
        };
        $scope.macsInputConfig = {
            searchField: ['text', 'value'],
            persist: true,
            create: false,
            maxItems: 1,
            render: {
                item: function (data, escape) {
                    return '<div>' + escape(data.text) + '</div>';
                }
            },
            onChange: function (value, oldValue) {
                $scope.macInputModel = value;
            }
        };
        $scope.syncMachineRules = function(){
            if (!$scope.macInputModel) {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            $scope.showSync = true;
            $scope.showStatus = 2;
            AuthorityRuleService.syncMachineRules($scope.app, mac[0], mac[1])
            .success((data) => {
                $scope.showStatus = data.success ? 0 : 1;
            })
            .error((data) => {
                $scope.showStatus = 1;
            });
        };

        function getMachineRules() {
            if (!$scope.macInputModel) {
                return;
            }
            let mac = $scope.macInputModel.split(':');
            AuthorityRuleService.queryMachineRules($scope.app, mac[0], mac[1])
                .success(function (data) {
                    if (data.code === 0 && data.data) {
                        $scope.loadError = undefined;
                        $scope.rules = data.data;
                        $scope.rulesPageConfig.totalCount = $scope.rules.length;
                    } else {
                        $scope.rules = [];
                        $scope.rulesPageConfig.totalCount = 0;
                        $scope.loadError = {message: data.msg};
                    }
                })
                .error((data, header, config, status) => {
                    $scope.loadError = {message: "????????????"};
                });
        };
        $scope.getMachineRules = getMachineRules;
        getMachineRules();

        var authorityRuleDialog;

        $scope.editRule = function (rule) {
            $scope.currentRule = angular.copy(rule);
              if(rule.ip){
                $scope.currentRule.macInputModel2=rule.ip+":"+rule.port;
              }else{
                $scope.currentRule.macInputModel2=":";
              }
            $scope.authorityRuleDialog = {
                title: '??????????????????',
                type: 'edit',
                confirmBtnText: '??????',
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.addNewRule = function () {
          $scope.currentRule.macInputModel2=":";
          var mac = $scope.currentRule.macInputModel2.split(':');
            $scope.currentRule = {
                app: $scope.app,
                ip: mac[0],
                port: mac[1],
                rule: {
                    strategy: 0,
                    limitApp: '',
                }
            };
            $scope.authorityRuleDialog = {
                title: '??????????????????',
                type: 'add',
                confirmBtnText: '??????',
                showAdvanceButton: true,
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.saveRule = function () {
          var mac = $scope.currentRule.macInputModel2.split(':');
          $scope.currentRule.ip=mac[0];
          $scope.currentRule.port=mac[1];
            if (!AuthorityRuleService.checkRuleValid($scope.currentRule.rule)) {
                return;
            }
            if ($scope.authorityRuleDialog.type === 'add') {
                addNewRuleAndPush($scope.currentRule);
            } else if ($scope.authorityRuleDialog.type === 'edit') {
                saveRuleAndPush($scope.currentRule, true);
            }
        };

        function addNewRuleAndPush(rule) {
            AuthorityRuleService.addNewRule(rule).success((data) => {
                if (data.success) {
                    getMachineRules();
                    authorityRuleDialog.close();
                } else {
                    alert('?????????????????????' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('?????????????????????' + data.msg);
                } else {
                    alert("?????????????????????????????????");
                }
            });
        }

        function saveRuleAndPush(rule, edit) {
            AuthorityRuleService.saveRule(rule).success(function (data) {
                if (data.success) {
                    getMachineRules();
                    if (edit) {
                        authorityRuleDialog.close();
                    } else {
                        confirmDialog.close();
                    }
                } else {
                    alert('?????????????????????' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('?????????????????????' + data.msg);
                } else {
                    alert("?????????????????????????????????");
                }
            });
        }

        function deleteRuleAndPush(entity) {
            if (entity.id === undefined || isNaN(entity.id)) {
                alert('?????? ID ????????????');
                return;
            }
            AuthorityRuleService.deleteRule(entity).success((data) => {
                if (data.code == 0) {
                    getMachineRules();
                    confirmDialog.close();
                } else {
                    alert('?????????????????????' + data.msg);
                }
            }).error((data) => {
                if (data) {
                    alert('?????????????????????' + data.msg);
                } else {
                    alert("?????????????????????????????????");
                }
            });
        };

        var confirmDialog;
        $scope.deleteRule = function (ruleEntity) {
            $scope.currentRule = ruleEntity;
            $scope.confirmDialog = {
                title: '??????????????????',
                type: 'delete_rule',
                attentionTitle: '?????????????????????????????????????????????',
                attention: '?????????: ' + ruleEntity.rule.resource + ', ????????????: ' + ruleEntity.rule.limitApp +
                    ', ??????: ' + (ruleEntity.rule.strategy === 0 ? '?????????' : '?????????'),
                confirmBtnText: '??????',
            };
            confirmDialog = ngDialog.open({
                template: '/app/views/dialog/confirm-dialog.html',
                scope: $scope,
                overlay: true
            });
        };

        $scope.confirm = function () {
            if ($scope.confirmDialog.type === 'delete_rule') {
                deleteRuleAndPush($scope.currentRule);
            } else {
                console.error('error');
            }
        };

        queryAppMachines();

        function queryAppMachines() {
            MachineService.getAppMachines($scope.app).success(
                function (data) {
                    if (data.code == 0) {
                        // $scope.machines = data.data;
                        if (data.data) {
                            $scope.machines = [];
                            $scope.macsInputOptions = [{
                                text:'??????',
                                value: ':'
                              }];
                            data.data.forEach(function (item) {
                                if (item.healthy) {
                                    $scope.macsInputOptions.push({
                                        text: item.ip + ':' + item.port,
                                        value: item.ip + ':' + item.port
                                    });
                                }
                            });
                        }
                        if ($scope.macsInputOptions.length > 0) {
                            $scope.macInputModel = $scope.macsInputOptions[0].value;
                        }
                    } else {
                        $scope.macsInputOptions = [{
                            text:'??????',
                            value: ':'
                          }];
                    }
                }
            );
        };
        $scope.$watch('macInputModel', function () {
            if ($scope.macInputModel) {
                getMachineRules();
            }
        });
    }]);