package cn.mnsj.cloud.rpc

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

// todo: 利用akka实现简易版spark通信框架 ---- Worker 端
class Worker(val memory:String,val cores:String,val masterHost:String,val masterPort:String) extends Actor{
  println("worker启动")


  // 定义workId
  val workerId=UUID.randomUUID.toString

  // 定义发送心跳间隔
  val SEND_HEART_BEAT_INTERVAL=10000 // 10s


  // 定义全局变量
  var master:ActorSelection=_

  override def preStart(): Unit = {
    master= context.actorSelection(s"akka.tcp://masterActorSystem@$masterHost:$masterPort/user/masterActor")
    // 发送注册信息
    master ! RegisterMessage(workerId,memory,cores)
  }

  override def receive: Receive = {
    // 接收注册成功的信息
    case RegistedMessage(msg)=>{
      println(msg)
      // 注册成功后 定期向master发送心跳
      import context.dispatcher
      // 设置定时任务 发送心跳给master  这时候需要自己给自己发送一个消息
      context.system.scheduler.schedule(0 millis,SEND_HEART_BEAT_INTERVAL millis,self,HeartBeat)
    }
      // 主动发送心跳
    case HeartBeat=>{
      // 向master发送心跳
      master ! SendHeartBeat(workerId)
    }
  }
}

object Worker{
  def main(args: Array[String]): Unit = {
    val host=args(0)
    val port=args(1)

    //定义worker内存
    val memory =args(2)
    //定义worker的核数
    val cores=args(3)
    //定义master的ip地址
    val masterHost=args(4)
    //定义master的port端口
    val masterPort=args(5)

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
    val workerActorSystem = ActorSystem("workerActorSystem",config)
    // 2. 通过ActorSystem创建master actor
    val workerActor:ActorRef = workerActorSystem.actorOf(Props(new Worker(memory,cores,masterHost,masterPort)),"workerActor")
  }
}