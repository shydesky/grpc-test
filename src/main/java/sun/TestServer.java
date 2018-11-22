package sun;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;

public class TestServer {

  private static final Logger logger = Logger.getLogger(TestServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should listen */
    int port = 50052;

    //boot server
    server = ServerBuilder.forPort(port)
        .addService(new TestImpl())
        .build()
        .start();

    //jvm回调钩子的作用，Runtime.getRuntime()可以获得java一些运行期间的一些信息。
    // 不管程序是正常关闭还是异常终端，在jvm关闭的时候做一些清理工作
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      TestServer.this.stop();
      System.err.println("*** server shut down");
    }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  static class TestImpl extends TestServiceGrpc.TestServiceImplBase {

    //通过继承实现rpc中定义的方法。
    @Override
    public void testFunction(testProto.InputParam req,
        StreamObserver<testProto.OutputParam> responseObserver) {
      logger.info(String.format("Receive Request From Client:%s", req.getKey()));
      testProto.OutputParam reply = testProto.OutputParam.newBuilder()
          .setKey(String.format("Hello, %s!", req.getKey())).build();
      responseObserver.onNext(reply);  //返回响应
      responseObserver.onCompleted();  //表明处理完毕
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    final TestServer server = new TestServer();
    logger.info("start grpc server");
    //启动server
    server.start();
    //block Server防止关闭
    server.blockUntilShutdown();
  }
}
