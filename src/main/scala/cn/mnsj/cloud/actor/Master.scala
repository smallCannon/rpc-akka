package cn.mnsj.cloud.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class Master extends Actor{
  //构造方法执行
  println("Master constructor invoked")

  // prestart在构造代码块之后被调用 只能被调用一次


  override def preStart(): Unit =  {
    println("preRestart method invoked")
  }

  override def receive: Receive = {
    case "connect" =>{
      println("Client Connected Successful !!!")
      // 发送注册成功信息给worker
      sender ! "success"
    }
  }
}


object Master{

  def main(args: Array[String]): Unit = {
    //master ip地址
    val host="192.168.92.30"
    //master 端口
    val port="8888"

    // 准备配置文件信息
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
