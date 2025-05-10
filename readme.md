## 재고 관리 시스템 동시성 처리 방법

> [블로그 링크](https://velog.io/@woongi9/%EC%9E%AC%EA%B3%A0-%EA%B4%80%EB%A6%AC-%EB%8F%84%EB%A9%94%EC%9D%B8%EC%97%90%EC%84%9C%EC%9D%98-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%9D%B4%EC%8A%88-%ED%95%B4%EA%B2%B0)

> 현재 **매장의 재고 관리 서비스**를 하고있는 스타트업에 서버 개발자로 일하고 있다. 새로운 프로젝트인 **식자재 쇼핑몰**을 시작하고, 개선 중인 중에 고민이 생겼다.

기존에 **매장 재고 관리**에서는 _한 매장에서 주문이 동시에 들어오거나 동시에 입고되는 등의 일이 드물어 동시성을 딱히 고민하지 않았다._

하지만 이번 식자재 쇼핑몰 프로젝트에서는 나중에 이벤트를 통한 쿠폰 발급 같은 것도 신경을 써달라는 말을 듣기는 했다. (다소 의역)

그럼 어떻게 ***동시성 이슈***를 해결할 수 있을지 먼저 알아보고 선택하자.

<br></br>

---

<br></br>

## 상황

> 100개의 재고 차감 요청이 동시에 들어온다.

![](https://velog.velcdn.com/images/woongi9/post/ca156a8b-7012-4f71-8831-47e87af9156d/image.png)

테스트코드는 이렇게 작성했다.

간단하게 코드를 설명하면
* `ExecutorService`
    * 비동기로 실행하는 작업을 단순하게 사용하게 도와주는 자바 API
* `CountDownLatch`
    * 다른 스레드에서 수행중인 요청이 끝날때까지 기다려주는 메소드

![](https://velog.velcdn.com/images/woongi9/post/ef35fdc6-5c77-4b2b-97fd-74f05e6faf8b/image.png)

기존의 단순한 차감 형태의 코드에서는 `잔여 수량` 검증 로직이 맞지 않는다.

그 이유는
<span style="color: blue">**RaceCondition**</blue>
* 둘 이상의 엑세스가 공유 데이터에 동시 변경하고 조회할때 생기는 문제
* 하나의 쓰레드가 완료되고 다음 쓰레드 접근


*어떻게 해결할 수 있을까?*

<bR></br>

---

<br></br>

## Synchronized

잘 알고있는 해당 차감 메소드에 동기화 선언을 해준다.

![](https://velog.velcdn.com/images/woongi9/post/83e5e2a1-6332-4a83-b752-c01b4ed67284/image.png)

메소드에 선언하고 다시 테스트를 실행하면

![](https://velog.velcdn.com/images/woongi9/post/3eb83b1d-e36f-4b85-9c2a-ec99fa2600e8/image.png)

테스트는 실패한다.
이유는 스프링의 `@Transactional` 동작 방식 때문인데
**트랜잭션 종료 전에 메서드 호출이 가능해서 문제가 발생**한다.

그러기에 `@Transactional`을 주석 처리하면
![](https://velog.velcdn.com/images/woongi9/post/ded59e30-97b8-45de-97b3-5ba67ff302eb/image.png)

![](https://velog.velcdn.com/images/woongi9/post/6c6267e5-6693-471f-9430-a67c30c29b2a/image.png)

테스트 케이스는 성공한다.

그러나
동기화는 하나의 프로세스 안에서만 보장된다.
서버가 여러대일 때는 실패할 수 밖에 없다.

<br></br>

## MySQL

그러면 MySQL의 락으로 해결해보자

### Pessimistic Lock
>
* **실제로 데이터에 락을 걸어서 정합성을 맞추는 방법.**
* exclusive lock 을 걸게되면 다른 트랜잭션에서는 **lock이 해제되기 전에 데이터를 가져갈 수 없음**
* **로우나 테이블 단위 락**
* **데드락이 걸릴 수 있으므로 주의**

![](https://velog.velcdn.com/images/woongi9/post/b7b90c16-d234-4493-8df8-92d23a124b70/image.png)

![](https://velog.velcdn.com/images/woongi9/post/f1367e5b-8ee7-48ef-bb7e-35e9bd508555/image.png)

**장단점**
* 충돌이 빈번하다면 `Optimistic Lock` 보다 성능이 좋다.
* 락을 통해 업데이트 제어해 **데이터 정합성**을 지킬 수 있다.

* 별도의 락을 잡기 때문에 성능 감소가 있을 수 있다.

<br></br>

### Optimistic Lock
>
* 실제로 락을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법.
* 데이터를 읽은 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트
* 내가 읽은 버전에서 수정사항이 생겼을 경우 application에서 다시 읽은 후에 작업을 수행

![](https://velog.velcdn.com/images/woongi9/post/7c951af8-36c1-4815-8f54-213ad21872da/image.png)

엔티티에 version 어노테이션 추가

![](https://velog.velcdn.com/images/woongi9/post/151f89d1-0456-4715-bc2d-517abe2af3c4/image.png)
![](https://velog.velcdn.com/images/woongi9/post/3a41af9f-728b-4894-8186-43b8d0147bac/image.png)

재시도 기능 분리를 위한 퍼사드 구현 및 실행

![](https://velog.velcdn.com/images/woongi9/post/c40cb675-724b-4173-bd93-79e0df42cab1/image.png)
(CPU 99.9%까지 도달해서 테스트 검증은 중지)

**장단점**
* 재시도 로직으로 이전보다 오래 걸림
* 별도의 락을 가지지 않아 **성능 상 `Pessimistic` 락보다 이점**
* 재시도 로직 개발자가 직접 작성

<br></br>

### Named Lock
>
* 이름을 가진 `metadata locking`
* 이름을 가진 락을 획득후 해제할 때까지 다른 세션은 이 lock을 획득할 수 없도록 합니다.
* `transaction`이 종료될 때 lock이 자동으로 해제되지 않습니다.
  -> 별도의 명령어로 해제를 수행하거나 선점시간이 끝났을 경우 해제
* `메타데이터` 단위 락


`Perssimistic Lock`과 `Named Lock`은 비슷해보이지만
**`Perssimistic Lock`은 로우나 테이블 단위 락이고 `Named Lock`은 메타데이터 단위 락**


![](https://velog.velcdn.com/images/woongi9/post/f7376fba-17ce-4163-ab16-7980caa40978/image.png)

![](https://velog.velcdn.com/images/woongi9/post/f34f1131-e60a-42d9-a206-3dffd99af1ea/image.png)
퍼사드 생성

![](https://velog.velcdn.com/images/woongi9/post/9d9162e6-016f-4356-8503-0b0421d387c5/image.png)
부모(퍼사드)의 트랜잭션과 별도로 실행되기 위해 `propagation` 실행

![](https://velog.velcdn.com/images/woongi9/post/9edd14bf-c0f5-42cc-bc87-653b286839c1/image.png)

같은 데이터 소스 사용으로 커넥션 풀 최대 설정 수정

![](https://velog.velcdn.com/images/woongi9/post/550de185-7729-477e-a8bb-fff0b91220d4/image.png)

**장단점**

* `Named락`은 **주로 분산락 구현시 사용**
* `Pessimistic` 락은 타임 아웃 구현이 힘들지만 타임 아웃 설정 가능

* 락 해제를 잘 해야돼서 구현이 어려움

<br></br>

---

<br></br>

## Redis

#### 설치방법

![](https://velog.velcdn.com/images/woongi9/post/b67fa412-cf60-4ea9-b785-61935fc45a78/image.png)

도커를 통한 레디스 설치

![](https://velog.velcdn.com/images/woongi9/post/5e4e1652-66f5-4c0f-9dd8-61e18ad76ff8/image.png)

```bash
docker pull redis

# redis 6379 기본 포트로 사용
docker run --name myredis -d -p 6379:6379 redis

# 
docker ps
```

레디스 실행


![](https://velog.velcdn.com/images/woongi9/post/6f6c69b9-dbb1-45a6-ade1-2c1717651bed/image.png)

`gradle`에 redis 의존성 추가

![](https://velog.velcdn.com/images/woongi9/post/9a8ac690-d5dd-4849-bd04-eb47fcdedb10/image.png)

![](https://velog.velcdn.com/images/woongi9/post/2704ded0-1c5d-40fe-b175-6b9ff1ef0f13/image.png)


`dockr ps` 명령어의
redis 컨테이너 아이디 복사
redis-cli 실행



### Lettuce
>
* setnx 명령어를 활영하여 **분산락** 구현
* **spin 락 방식** (Named Lock과 유사)
* 락 획득 못 했을때 **별도의 재시도** 필요
* 레디스 이용한다는 점과 세션 관리 신경 안 써도 됨


![](https://velog.velcdn.com/images/woongi9/post/6a7f8239-9a74-4591-ae80-fc48fcf11294/image.png)
Lock 관리를 위한 redisRepository 생성

![](https://velog.velcdn.com/images/woongi9/post/dacc3d80-a84b-4242-bb09-afbd54a167c0/image.png)
퍼사드 생성

1. `NamedLock` 방식과 동일해서 `NamedLock` 서비스 구현체 의존성 주입
2. 반복된 접근은 레디스에 **부하를 주기에 실패시 0.1초 슬립으로 락 획득에 텀 추가**

![](https://velog.velcdn.com/images/woongi9/post/91839199-5e2b-4cd9-a9cb-3d28aa6c87fc/image.png)

<br></br>

### Redisson
>
* **pub-sub** 기반으로 락 구현 제공

![](https://velog.velcdn.com/images/woongi9/post/8885777c-22b7-4b26-ad1b-baaed902380d/image.png)
1. mvnrepository.com 접속
2. redisson 검색
3. Redisson/Spring Boot Starter 접속
4. 의존성 복사

![](https://velog.velcdn.com/images/woongi9/post/93d2c923-bec4-4122-927e-f3e32972de52/image.png)

RedissonLockFacade 추가

![](https://velog.velcdn.com/images/woongi9/post/98bb3775-f714-45eb-83f9-0d58b6d13cff/image.png)

* **pub-sub** 구현이기에 레디스의 부하를 줄여줌
* 구현 복잡
* 별도 라이브러리 사용해야함


<br></br>

---

<br></br>

## 결론

> 어떤걸 쓰는게 맞는걸까?
내가 내린 결론은 서비스마다 다르다이다.

하지만 서비스 특성상 B2B이고, 많은 유저가 동시에 트래픽이 몰리는 걱정을 할 정도는 아니라고 생각이 들어 `Pessimistic Lock` 을 사용하였고, 후에 더 많은 트래픽이나 이슈가 발생하면 `Optimistic Lock`을 적용하고 더 나아가 레디스 사용을 고려하려고 한다.
(레디스를 너무 써보고 싶다.)

<br></br>

