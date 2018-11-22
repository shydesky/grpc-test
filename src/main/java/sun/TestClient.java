package sun;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class TestClient {

  private static final Logger logger = Logger.getLogger(TestClient.class.getName());

  private TestServiceGrpc.TestServiceBlockingStub blockingStub;
  private TestServiceGrpc.TestServiceStub asyncStub;

  public TestClient() {
    // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid needing certificates.
    Channel channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext()
        .build();

    //创建阻塞类型的stub
    blockingStub = TestServiceGrpc.newBlockingStub(channel);
    asyncStub = TestServiceGrpc.newStub(channel);
  }

  public void testFunctionBlock(String name) {
    testProto.InputParam request = testProto.InputParam.newBuilder().setKey(name).build();
    testProto.OutputParam response;
    //调用方法
    response = blockingStub.testFunction(request);
    logger.info("Receive Response From Server:" + response.getKey());
  }

  public void testFunctionAsync(String name) throws InterruptedException {
    testProto.InputParam request = testProto.InputParam.newBuilder().setKey(name).build();
    testProto.OutputParam response;
    final CountDownLatch latch = new CountDownLatch(1); // using CountDownLatch

    StreamObserver<OutputParam> responseObserver = new StreamObserver<OutputParam>() {
      @Override
      public void onNext(OutputParam value) {
        logger.info("Receive Response From Server:" + value.toString());
      }

      @Override
      public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        logger.info("failed with status : " + status );
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        logger.info("finished!");
        latch.countDown();
      }
    };

    //调用方法
    asyncStub.testFunction(request, responseObserver);
    latch.await();
  }

  public static void main(String[] args) throws Exception {
    TestClient client = new TestClient();
    try {
      String user = "shy";
      client.testFunctionBlock(user);  //阻塞调用
      client.testFunctionAsync(user);  //异步调用
    } finally {

    }
  }
}
