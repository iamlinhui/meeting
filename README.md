```text
init-method="init"
    说明在创建完对象后，立刻执行init方法，用来进行初始化
destroy-method="destroy"
    当该bean为单例模式，才能调用该方法 destroy方法在容器销毁的时候被调用
    当该bean为多例时，spring容器不负责容器的销毁工作
    如果该bean为多例时，当不用该bean时应该手动的销毁
    
Bean在实例化的过程中：Constructor > @PostConstruct >InitializingBean > init-method
Bean在销毁的过程中：@PreDestroy > DisposableBean > destroy-method
```