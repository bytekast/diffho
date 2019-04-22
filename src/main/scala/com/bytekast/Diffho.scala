package com.bytekast

import java.io.File
import java.util.Map

import scala.collection.JavaConverters._
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigFactory
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import org.fusesource.jansi.Ansi._

import scala.collection.mutable


@CommandLine.Command(name = "Diffho", version = Array("0.0.1"),
  mixinStandardHelpOptions = true, // add --help and --version options
  description = Array("Diff Utility for HOCON files"))
class Diffho extends Runnable {

  @picocli.CommandLine.Parameters(arity = "1", paramLabel = "CONFIG FILES", description = Array("The configs to compare"))
  private val configs: Array[File] = null

  override def run(): Unit = {
    //AnsiConsole.systemInstall()

    def colorize = (color: String, text: String) => ansi().render(s"@|$color $text|@")

    if (configs.size != 2) {
      System.err.println(colorize("red", "ERROR: You must provide exactly 2 config files"))
      System.exit(1)
    }

    if (!configs.head.exists()) {
      System.err.println(colorize("red", "ERROR: ${configs.first()} does not exist"))
      System.exit(1)
    }

    if (!configs.last.exists()) {
      System.err.println(colorize("red", "ERROR: ${configs.last()} does not exist"))
      System.exit(1)
    }

    try {

      val leftConfig = ConfigFactory.parseFile(configs.head).entrySet().asScala
      val rightConfig = ConfigFactory.parseFile(configs.last).entrySet().asScala

      val common = leftConfig intersect rightConfig
      val diff = ((leftConfig ++ rightConfig) -- common)

      val leftKeys = leftConfig.map(_.getKey)
      val rightKeys = rightConfig.map(_.getKey)

      val left = diff.filter(v => leftKeys.contains(v.getKey) && !rightKeys.contains(v.getKey))
      val right = diff.filter(v => !leftKeys.contains(v.getKey) && rightKeys.contains(v.getKey))
      val both = (diff -- left -- right).groupBy(_.getKey)

      if (left.nonEmpty) {
        clearScreen
        println(colorize("green", "\n------------- LEFT ADDITIONS-------------\n"))
        left.foreach(it => println(s"${it.getKey} => ${colorize("green", it.getValue.render())}"))
        println(colorize("yellow", "\n\nPress Enter to continue"))
        System.in.read()
      }

      if (right.nonEmpty) {
        clearScreen
        println(colorize("green", "\n------------- RIGHT ADDITIONS-------------\n"))
        right.foreach(it => println(s"${it.getKey} => ${colorize("blue", it.getValue.render())}"))
        println(colorize("yellow", "\n\nPress Enter to continue"))
        System.in.read()
      }

      def findLeft = (list: mutable.Set[java.util.Map.Entry[String, ConfigValue]]) => {
        if (leftConfig.contains(list.head)) list.head.getValue else list.last.getValue
      }

      def findRight = (list: mutable.Set[java.util.Map.Entry[String, ConfigValue]]) => {
        if (rightConfig.contains(list.head)) list.head.getValue else list.last.getValue
      }

      if (both.nonEmpty) {
        clearScreen
        println(colorize("red", "\n------------- MERGE CONFLICTS -------------\n"))
        for ((it, idx) <- both.zipWithIndex) {
          println(s"key: ${colorize("red", it._1)}")
          println(s"left: ${colorize("green", findLeft(it._2).render())}")
          println(s"right: ${colorize("blue", findRight(it._2).render())}")
          println("\n")
          if (idx != 0 && idx % 5 == 0) {
            println(colorize("yellow", "\n\nPress Enter to continue"))
            System.in.read()
            clearScreen
            println(colorize("red", "\n------------- MERGE CONFLICTS -------------\n"))
          }
        }
      }

    } catch {
      case e: Throwable => {
        System.err.println("red", e.getMessage)
        System.exit(1)
      }
    }
  }

  private def clearScreen = {
    print("\u001b[H\u001b[2J")
    System.out.flush()
  }
}

object Diffho {
  def main(args: Array[String]) {
    CommandLine.run(new Diffho(), args: _*)
  }
}