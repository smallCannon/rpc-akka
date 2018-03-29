package cn.mnsj.cloud.rpc

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

// todo: 利用akka实现简易版spark通信框架 ---- Master端
class Master extends Actor{
  println("Master 启动。。")

  // map集合  用来保存worker存活节点的属性 key为workerId value为对应的对象
  val workerMap = new mutable.HashMap[String,WorkerInfo]()
  // list集合 用来保存所有存活的woker信息
  val workerList=ListBuffer[WorkerInfo]()
  // master定时检查时间间隔
  val CHECK_TIMEOUT_INTERVAL=15000 //15秒

  override def preStart(): Unit = {
    println("master preStart invoked")
    import context.dispatcher
    // scala定时器
    // millis 需要导入包 scala.concurrent.duration._
    //  需要导入context,dispatcher
    context.system.scheduler.schedule(0 millis,CHECK_TIMEOUT_INTERVAL millis,self,CheckOutTime)
  }

  // receive方法会在preStart方法后被调用，表示不断接收新的消息
  // 模式匹配
  override def receive: Receive = {
    // 注册信息匹配
    case RegisterMessage(workerId,memory,cores)=>{
        if(!workerMap.contains(workerId)) {
          val workerinfo = new WorkerInfo(workerId, memory, cores)
          // 保存到Map中  以 workerId 作为key
          workerMap.put(workerId, workerinfo)
          // 保存到list中
          workerList +=workerinfo
          println(s"workerId:$workerId 连接成功")
          sender ! RegistedMessage(s"$workerId 注册成功")
        }
    }

     // master 接收 worker 发送过来的心跳信息
    case SendHeartBeat(workerId) =>{
      // 判断是否注册
      if(workerMap.contains(workerId)) {
        // 获取worker信息
          val info:WorkerInfo = workerMap(workerId)
         //获取当前系统时间
          val lastTime =System.currentTimeMillis()
          info.lastHeartBeatTime=lastTime
      }
    }

      // 检查死亡的节点  通过判断workerInfo的lastHeartBeatTime属性和当前的时间进行比较  超过十五秒 算是该挂掉
      // 因为 每过十秒钟 worker 节点就会向master节点发送心跳并且更新workerInfo的lastHeartBeatTime
    case CheckOutTime=>{
      println("开始过滤超时的worker")
      // 过滤出超时的woker 获取系统当前时间 跟wokerInfo中的lastheartBeatTime 进行逻辑比较
      val outTimeWoker: ListBuffer[WorkerInfo] = workerList.filter(System.currentTimeMillis()-_.lastHeartBeatTime>CHECK_TIMEOUT_INTERVAL)
      // 遍历超时的worker信息  然后移除
      for(wokerinfo <- outTimeWoker) {
          val wokerId =wokerinfo.wokerId
          // map通过key移除
          workerMap.remove(wokerId)
          // list移除
          workerList -= wokerinfo
          println(s"超时的workerId:$wokerId")
      }
      println("活着的worker总数:"+workerList.size)
      // master 按照woker内存大小进行降序排列  reverse
      println(workerList.sortBy(_.memery).reverse.toList)
    }
  }
}


object Master{
  def main(args: Array[String]): Unit = {
    // master 的IP
    val host =args(0)
    // master 的端口
    val port=args(1)

    val configStr=
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |
      """.stripMargin

    // 利用ConfigFactory解析配置文件，获取配置信息
    val config = ConfigFactory.parseString(configStr)

    // 1. 创建ActorSystem 他是整个进程中老大 它负责创建和监督actor 单例
    val masterActorSystem = ActorSystem("masterActorSystem",config)
    // 2. 通过ActorSystem创建master actor
    val masterActor:ActorRef = masterActorSystem.actorOf(Props(new Master),"masterActor")

  }
}
