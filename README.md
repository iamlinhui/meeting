```text
init-method="init"
    ˵���ڴ�������������ִ��init�������������г�ʼ��
destroy-method="destroy"
    ����beanΪ����ģʽ�����ܵ��ø÷��� destroy�������������ٵ�ʱ�򱻵���
    ����beanΪ����ʱ��spring�������������������ٹ���
    �����beanΪ����ʱ�������ø�beanʱӦ���ֶ�������
    
Bean��ʵ�����Ĺ����У�Constructor > @PostConstruct >InitializingBean > init-method
Bean�����ٵĹ����У�@PreDestroy > DisposableBean > destroy-method
```