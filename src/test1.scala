// Simple server
import java.io._
import java.net.{InetAddress, Socket, _}

import scala.io.{BufferedSource, Source}

object test1 extends App {
  val node1 = new Node(8000, List(), "./files1/")
  val node2 = new Node(8001, List(Address("localhost", 8000)), "./files2/")

  new Thread(node1).start
  new Thread(node2).start
}

class Node(port: Int, neighbours: List[Address], directory: String) extends Runnable {
  def run {
    println("Startup: " + port)
    val server = new ServerSocket(port)

    this.sendFile("test.txt")

    while (true) {
      val s = server.accept()
      val in = new BufferedSource(s.getInputStream()).getLines()
      if (in.hasNext) {
        val filePath = in.next()
        val writer = new PrintWriter(new File(directory + "" + filePath))

        while (in.hasNext) {
          writer.println(in.next())
        }

        writer.close()
      }
    }
  }

  def startMessage(num: Int) {
    for (elem <- neighbours) {
      val peer = new Socket(InetAddress.getByName(elem.ip), elem.port)
      val out = new PrintStream(peer.getOutputStream())
      out.println(num)
      out.flush()

      peer.close()
    }
  }

  def sendFile(path: String): Unit = {
    for (elem <- neighbours) {
      val lines = Source.fromFile(directory + "" + path).getLines
      val peer = new Socket(InetAddress.getByName(elem.ip), elem.port)
      val printStream = new PrintStream(peer.getOutputStream)

      printStream.println(path)
      lines.foreach {
        printStream.println
      }

      printStream.flush()
      peer.close()
    }
  }
}