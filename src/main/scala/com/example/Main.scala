package com.example

import distage.{Tag, _}
import zio._
import zio.actors.Actor.Stateful
import zio.actors.{Supervisor, _}
import zio.console._

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

    val program = for {
      system <- ZIO.service[ActorSystem]
      actor <- system.make("actor1", Supervisor.none, (), stateful)
      doubled <- actor ! DoubleCommand(42)
      _ <- putStrLn(s"$doubled")
    } yield ()

    def provideHas[R: HasConstructor, A: Tag](fn: R => A): Functoid[A] =
      HasConstructor[R].map(fn)

    val module = new ModuleDef {
      make[ActorSystem].fromResource(ActorSystem("processing").toManaged(as => as.shutdown.ignore))
      make[Task[Unit]].from(provideHas(program.provide))
    }

    Injector[Task]()
      .produceGet[Task[Unit]](module)
      .useEffect
      .exitCode
  }
}
