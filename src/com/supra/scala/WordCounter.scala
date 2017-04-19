package com.supra.scala
import org.apache.spark._
import scala.io.Source
import org.apache.spark.SparkContext._
import org.apache.log4j._

/**
  * Created by Supra on 30/12/2016.
  */
object WordCounter extends java.io.Serializable{

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    var stopwords: Set[String] = Set()
    val stopis = Source.fromFile("../SparkScalaWorkshop/data/stop_words.txt").getLines.toList
    for(stop <- stopis){
      stopwords+=stop
    }

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "WordCounter")

    // Read each line of input data
    val lines = sc.textFile("../SparkScalaWorkshop/data/book.txt")

    // Read stop words
    val broadcastedVariable = sc.broadcast(stopwords)

    // Seperate by space and convert to lower case
    val words = lines.flatMap(x=>x.split("\\W+")).map(x=>x.toLowerCase()).filter(!broadcastedVariable.value(_))

    // Convert to (stationID, temperature)
    val counts = words.map(x => (x,1))

    // Finally the count of individual words
    val countResults = counts.reduceByKey(_+_)

    // Flip the count results value as key, key as value
    val countResultsRefined = countResults.map(x => (x._2,x._1)).sortByKey()

    // Collect, format, and print the results
    val results = countResultsRefined.collect()

    for (result <- results) {
      println(result)
    }

  }

}
