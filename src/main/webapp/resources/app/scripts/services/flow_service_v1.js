var app = angular.module('sentinelDashboardApp');

app.service('FlowServiceV1', ['$http', function ($http) {
    this.syncMachineRules = function(app, ip, port) {
         var param = {
             ip: ip,
             port: port
         };
         return $http({
             url: '/v1/flow/rules/' + app,
             params: param,
             method: 'GET'
         });
     };
    this.queryMachineRules = function (app, ip, port) {
        var param = {
            app: app,
            ip: ip,
            port: port
        };
        return $http({
            url: '/v1/flow/rules',
            params: param,
            method: 'GET'
        });
    };

    this.newRule = function (rule) {
        var param = {
            resource: rule.resource,
            limitApp: rule.limitApp,
            grade: rule.grade,
            count: rule.count,
            strategy: rule.strategy,
            refResource: rule.refResource,
            controlBehavior: rule.controlBehavior,
            warmUpPeriodSec: rule.warmUpPeriodSec,
            maxQueueingTimeMs: rule.maxQueueingTimeMs,
            app: rule.app,
            ip: rule.ip,
            port: rule.port
        };

        return $http({
            url: '/v1/flow/rule',
            data: rule,
            method: 'POST'
        });
    };

    this.saveRule = function (rule) {
        var param = {
            id: rule.id,
            resource: rule.resource,
            limitApp: rule.limitApp,
            grade: rule.grade,
            count: rule.count,
            strategy: rule.strategy,
            refResource: rule.refResource,
            controlBehavior: rule.controlBehavior,
            warmUpPeriodSec: rule.warmUpPeriodSec,
            maxQueueingTimeMs: rule.maxQueueingTimeMs,
        };

        return $http({
            url: '/v1/flow/rule/'+rule.id,
            data: rule,
            method: 'PUT'
        });
    };

    this.deleteRule = function (rule) {
        var param = {
            id: rule.id,
            app: rule.app
        };

        return $http({
            url: '/v1/flow/delete.json',
            params: param,
            method: 'DELETE'
        });
    };

    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    function notNumberGreaterThanZero(num) {
        return num === undefined || num === '' || isNaN(num) || num <= 0;
    }

    this.checkRuleValid = function (rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('????????????????????????');
            return false;
        }
        if (rule.count === undefined || rule.count < 0) {
            alert('?????????????????????????????? 0');
            return false;
        }
        if (rule.strategy === undefined || rule.strategy < 0) {
            alert('?????????????????????');
            return false;
        }
        if (rule.strategy == 1 || rule.strategy == 2) {
            if (rule.refResource === undefined || rule.refResource == '') {
                alert('??????????????????????????????');
                return false;
            }
        }
        if (rule.controlBehavior === undefined || rule.controlBehavior < 0) {
            alert('???????????????????????????');
            return false;
        }
        if (rule.controlBehavior == 1 && notNumberGreaterThanZero(rule.warmUpPeriodSec)) {
            alert('???????????????????????? 0');
            return false;
        }
        if (rule.controlBehavior == 2 && notNumberGreaterThanZero(rule.maxQueueingTimeMs)) {
            alert('?????????????????????????????? 0');
            return false;
        }
        if (rule.clusterMode && (rule.clusterConfig === undefined || rule.clusterConfig.thresholdType === undefined)) {
            alert('???????????????????????????');
            return false;
        }
        return true;
    };
}]);
