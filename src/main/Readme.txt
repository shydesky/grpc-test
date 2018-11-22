本项目展示如何使用Google的开源框架gRPC搭建RPC服务

一、定义服务接口
   gRPC 默认使用 protocol buffers 作为接口定义语言，来描述服务接口和有效载荷消息结构。更多的proto3文档信息，参考https://developers.google.com/protocol-buffers/docs/proto3
   编写proto文件来定义rpc的服务接口，本项目中写在test.proto文件中。

   1、定义服务接口
   rpc服务接口的定义中不能使用基本类型，方法参数和返回值都必须是自定义的message类型。

   2、代码生成
     （1）生成消息对象和序列化、反序列化的代码。生成的文件是 testProto.java
      protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/test.proto
     （2）生成通信部分的代码。生成的文件是 TestServiceGrpc.java
      protoc --plugin=protoc-gen-grpc-java=/path/to/protoc-gen-grpc-java --grpc-java_out=$DST_DIR --proto_path=$SRC_DIR $SRC_DIR/test.proto

二、实现服务端 （编写的文件是TestServer.java）
   1、实现服务类。
   通过继承生成的基本服务类，并重写相应的RPC方法来完成具体的工作。

   2、启动服务，监听客户端的请求并返回信息
   （1）指定服务监听的端口;
   （2）创建具体的服务对象，并注册给 ServerBuilder;
   （3）创建 Server 并启动;

三、实现客户端 （编写的文件是TestClient.java）
  （1）创建stub。有两种类型的stub
       blocking/synchronous stub: 阻塞，客户端发起 RPC 调用后一直等待服务端的响应
       non-blocking/asynchronous stub: 非阻塞，异步响应，通过 StreamObserver 在响应时进行回调
  （2）调用stub中同名的方法。
       对于异步的 stub，则需要一个 StreamObserver 对象来完成回调处理。

总的来说，使用 gRPC 构建 RPC 分为三步：
  1）使用 IDL 定义服务接口及通信消息对象；
  2）使用 Protocol Buffers 和 gRPC 工具生成序列化/反序列化和 RPC 通信的代码；
  3）基于生成的代码创建服务端和客户端应用。
gRPC 在数据交换格式上使用了自家的 Protocol Buffers，已经被证明是非常高效序列化框架；在传输协议上 gRPC 支持 HTTP 2.0 标准化协议，比 HTTP 1.1 有更好的性能。

RPC 的实现原理其实是基于 C/S 架构的，通过网络将客户端的请求传输给服务端，服务端对请求进行处理后将结果返回给客户端。
在很多情况下使用 JSON 进行数据传输的 REST 服务和 RPC 实现的效果差不多，都是跨网络进行数据的交换，但是 RPC 中客户端在进行方法调用的时候更加便捷，底层是完全透明的，看上去就像是调用本地方法一样。