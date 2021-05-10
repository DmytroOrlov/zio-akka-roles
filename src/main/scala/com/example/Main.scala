package com.example

import zio._
import zio.actors.Actor.Stateful
import zio.actors._
import zio.console._

import java.io.File

sealed trait Command[+_]

case class DoubleCommand(value: Int) extends Command[Int]

object Main extends App {
  def run(args: List[String]) = {
    val stateful = new Stateful[Any, Unit, Command] {
      def receive[A](state: Unit, msg: Command[A], context: Context): UIO[(Unit, A)] =
        msg match {
          case DoubleCommand(value) => UIO(((), value * 2))
        }
    }

    val program =
      for {
        _ <- putStrLn("123")
      } yield ()

    program
      .exitCode
  }
}
