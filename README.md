# Sentinel-dashboard-X 实现分Sentinel高可用及规则持久

对于流控规则持久化，社区比较流行采用nacos进行配置管理，但实际上nacos配置最终还是存储在mysql等数据库config_info表中。

## 存在的问题
- 1、数据库中规则数据的修改同样不会被推送到sentinel客户端和dashboard中，需要在nacos配置界面点击发布。
- 2、增加了nacos服务器的压力。
- 3、引入nacos使系统变得复杂，耦合性变高，不能独立部署。
- 4、规则在dashboard应用内存中，随着接入应用增多服务器资源消耗存在压力。
- 5、dashboard基于内存不支持水平扩展。
- 6、规则绑定资源主机，app机器数成百上千时维护困难。
- 7、不支持规则手工实时同步到app主机。

## 解决方案
使用数据库作为规则持久方案。原因：
- 1、数据库可靠性有保证，稳定性较好。
- 2、数据库是数据存储的标配，通用性较好，扩展性方案成熟。
- 3、实现方案更简单，避免跨系统调用，独立可用。
- 4、dashboard只是一个控制台，内参消耗少，且支持负载均衡，水平扩展。
- 5、支持全局规则（ip，port为空）+ 主机规则相结合，更加灵活方便。

## 特性
- 1、支持应用规则rule，监控数据metric持久化到数据库。
- 2、dashboard只是一个控制台，基于消耗少，且支持负载均衡，水平扩展。
- 3、支持从dashoard发布的规则实时同步到sentinel客户端。
- 4、支持全局规则(app级)流控规则配置及同步。即配置【资源主机】为全部或空，则规则适用于app所有实例主机，并同步规则到所有主机。网关流控规则暂未实现。
- 5、支持app【资源主机】级流规则流控和同步。可单独对资源主机（ip + port）进行流控规则设置。
- 6、实时监控界面优化，增加查询区间。原来只查当前一分钟，左侧图表区间太小。
- 7、支持作为服务注册到nacos和从nacos获取dashboard配置
- 8、支持规则持久化到nacos。按官方指导已实现DynamicRuleProvider，DynamicRulePublisher，需改造controller，与InMem*Store配合使用，参考 FlowControllerV2。

## sentinel
[sentinel源码](https://github.com/alibaba/Sentinel)
