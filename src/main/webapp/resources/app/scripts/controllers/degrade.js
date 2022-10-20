var app = angular.module('sentinelDashboardApp');

app.controller('DegradeCtl', ['$scope', '$stateParams', 'DegradeService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, DegradeService, ngDialog, MachineService) {
    //初始化
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
        DegradeService.syncMachineRules($scope.app, mac[0], mac[1])
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
      var mac = $scope.macInputModel.split(':');
      DegradeService.queryMachineRules($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.rules = data.data;
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getMachineRules = getMachineRules;
    getMachineRules();

    var degradeRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      if(rule.ip){
        $scope.currentRule.macInputModel2 = rule.ip +":"+rule.port
      } else {
        $scope.currentRule.macInputModel2 = ":"
      }
      $scope.degradeRuleDialog = {
        title: '编辑熔断规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      $scope.currentRule = {
        resource: "/",
        grade: 0,
        app: $scope.app,
        limitApp: 'default',
        slowRatioThreshold: 0.7,
        timeWindow:2000,
        minRequestAmount: 5,
        statIntervalMs: 1000,
      };
      $scope.currentRule.macInputModel2=":";
      $scope.degradeRuleDialog = {
        title: '新增熔断规则',
        type: 'add',
        confirmBtnText: '新增'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      var mac = $scope.currentRule.macInputModel2.split(':');
      $scope.currentRule.ip=mac[0];
      $scope.currentRule.port=mac[1];
      if (!DegradeService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.degradeRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.degradeRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    function parseDegradeMode(grade) {
        switch (grade) {
            case 0:
              return '慢调用比例';
            case 1:
              return '异常比例';
            case 2:
              return '异常数';
            default:
              return '未知';
        }
    }

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除熔断规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下熔断规则',
        attention: '资源名: ' + rule.resource +
            ', 熔断策略: ' + parseDegradeMode(rule.grade) + ', 阈值: ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      DegradeService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          confirmDialog.close();
        } else {
          alert('失败：' + data.msg);
        }
      });
    };

    function addNewRule(rule) {
      DegradeService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          degradeRuleDialog.close();
        } else {
          alert('失败：' + data.msg);
        }
      });
    };

    function saveRule(rule, edit) {
      DegradeService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          if (edit) {
            degradeRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('失败：' + data.msg);
        }
      });
    }
    queryAppMachines();
    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code === 0) {
            // $scope.machines = data.data;
            if (data.data) {
              $scope.machines = [];
              $scope.macsInputOptions = [{
                text:'全部',
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
              text:'全部',
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