package com.supra.scala
import org.apache.spark._
import scala.io.Source
import org.apache.spark.SparkContext._
import org.apache.log4j._
/**
  * Created by Supra on 04/01/2017.
  */
object MostPopularSuperhero extends java.io.Serializable{

  /**
    * Notice there is an indefinite return type: Option which means If possible
    * return (Int, String) but if null then just ignore.
    * Option means instance of Some class or instance of None class
  */

  /**
    * The data looks like:
    * 1 "24-HOUR MAN/EMMANUEL"
    * 2 "3-D MAN/CHARLES CHAN"
    * Delimiting character '\"' is used to separate id and name, trim() means to
    * avoid space
    */
  def superheroNames(line: String) : Option[(Int, String)] = {
    var fields = line.split('\"')
    if(fields.length>1){
      return Some(fields(0).trim().toInt, fields(1))
    } else{
      return None
    }
  }

  /**
    * The data looks like in superhero graph:
    * 5988 748 1722 3752 4655 5743 1872 3413 5527
    * 6368 6085 4319 4728 1636 2397 3364 4001 1614
    * Here, the first number is superhero id and rest of it is (a list of friends
    * of that superhero by their ids)
    * fields.length - 1 explains the count of rest of the parts
    */

  def superheroGraph(line: String) = {
    var fields = line.split(" ")
    (fields(0).toInt, fields.length - 1)
  }

  def main(args: Array[String]){
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "MostPopularSuperHero")

    /**
      * Concept of flatMap vs map --> map does the same job as flatMap except does not
      * return the results as a complete list like flatMap
      */
    // Build up hero ID -> name RDD
    val names = sc.textFile("../SparkScalaWorkshop/data/Marvel-names.txt")
    val namesRDD = names.flatMap(superheroNames)

    // Build up graph RDD look it's only map because there is "None" return
    val graph = sc.textFile("../SparkScalaWorkshop/data/Marvel-graph.txt")
    val graphRDD = graph.map(superheroGraph)

    // Count the no of friends of a super hero, which spans more than one line
    val totalFriendsByCharacter = graphRDD.reduceByKey(_+_)

    // Flip the obtained results
    val flipped = totalFriendsByCharacter.map(x => (x._2,x._1))

    // Sort by key
    val sortedResults = flipped.sortByKey().top(10)

    for(a <- sortedResults){
      var count = a._1
      // With respect to th ID find the name only
      // Now lookup returns an array and we need the part(0) only
      var name = namesRDD.lookup(a._2)(0)
      println(s"$name with $count co-appearances.")
    }
  }
}
