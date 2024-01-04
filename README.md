# Kafka 성능 테스트

![image](https://github.com/siawase7179/Kafka/assets/152139618/fcc4c2d9-3e38-465a-8898-f423f77a0150)


## 1. 기본 테스트

![image](https://github.com/siawase7179/Kafka/assets/152139618/08495050-acfa-4398-bf50-c42da3d424d0)

Producer 1개 App, Counser 1개 App을 의미

> [!note]
> Producer 성능이 초당 publish 건수가 10만건 이상 나왔으나, Consumer의 속도가 Producer를 따라가지 못했다.

-------------

## 2. 파티셔닝 사용

**Consumer 성능을 올리기 위해 Partition 기능 사용**

![image](https://github.com/siawase7179/Kafka/assets/152139618/5630d023-d769-46c3-9f50-41c414bd5e3c)

> [!note]
> 동일한 Consumer 그룹 내 Consumer가 추가되면 각 Consumer가 가지는 Partition의 소유권이 바뀌게 된다.
>
> 이렇게 소유권이 이동하는 것을 리밸런스 rebalance 라고 한다.
>
> Consumer 그룹의 리밸런스를 통해 Consumer 그룹에는 Consumer를 쉽고 안전하게 추가할 수 있고 제거할 수도 있어 높은 가용성과 확장성을 확보할 수 있다.
>
> 리밸런스를 하는 동안 일시적으로 Consumer는 메시지를 가져올 수 없다는 단점이 있다.
>
> 참고 : https://joooootopia.tistory.com/30(https://joooootopia.tistory.com/30)


**즉, Partiton 하나당 하나의 Consumer group 의 Consumer 만 접근할 수 있다. 아직 Consumer의 속도가 너무 느리다.**

-------------

## 3. Consumer 수 조정

![image](https://github.com/siawase7179/Kafka/assets/152139618/abcd5d54-352d-4d99-9a7c-86ba359a47aa)

Consumer 성능을 올리기 위해 Partition 기능 + Consumer 당 하나의 Partition에 접근 할 수 있도록 처리

> [!note]
> Producer 하나가 10만건씩 Publish하는데도 Consumer의 속도가 밀리지 않는다.
>
> Publish 10만건 Consumer 속도도 10만건 씩 나온다.

-------------

## 4. RabbitMQ와 비교

![image](https://github.com/siawase7179/Kafka/assets/152139618/d29ca2a9-b5fb-4e0f-aa18-a29bedf77c86)

> [!note]
> Kafka 파티션과 consumer 수가 성능에 큰 영향을 미친다.
>
> 당연한 얘기지만, 그렇다고 너무 많은 파티션은 클러스터 부하를 준다.
>
> 적절한 파티션 수를 선택하는 것이 중요하다.
>
> 파티션 수를 결정할 때는 클러스터의 크기, 데이터의 양과 특성, 컨슈머의 수와 성능 등을 고려해야 한다.
>
> 성능에서는 확실히 Kafa가 큰 우위를 가지지만 RabbitMQ 기능은 아쉬움이 남는다.
>
> 메시지 큐 선택 시 고려할 점이다.
