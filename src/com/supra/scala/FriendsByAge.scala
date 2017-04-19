package com.supra.scala

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

/**
  * Data looks like,
  * 0,Will,33,385
  * 1,Jean-Luc,26,2
  * 2,Hugh,55,221
  * 3,Deanna,40,465
  * */
/** Compute the average number of friends by age in a social network. */
object FriendsByAge extends java.io.Serializable{

  /** A function that splits a line of input into (age, numFriends) tuples. */
  def parseLine(line: String) = {
    // Split by commas
    val fields = line.split(",")
    // Extract the age and numFriends fields, and convert to integers
    val age = fields(2).toInt
    val numFriends = fields(3).toInt
    // Create a tuple that is our result.
    (age, numFriends)
    //val name = fields(1)
    //name
  }

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "FriendsByAge")

    // Load each line of the source data into an RDD
    val lines = sc.textFile("../SparkScalaWorkshop/data/fakefriends.csv")

    // Use our parseLines function to convert to (age, numFriends) tuples
    val rdd = lines.map(parseLine)

    // Lots going on here...
    // We are starting with an RDD of form (age, numFriends) where age is the KEY and numFriends is the VALUE
    // We use mapValues to convert each numFriends value to a tuple of (numFriends, 1)
    // Then we use reduceByKey to sum up the total numFriends and total instances for each age, by
    // adding together all the numFriends values and 1's respectively.
    /**
      * Here,
      * 23,Runa,33,285
      * 24,Yasir,30,21
      * 25,Omar,33,2
      * Converted to:
      * rdd.mapValues(x=>(x,1))
      * (33,(285,1))
      * (33,(2,1))
      * rdd.mapValues(x=>(x,1)).reduceByKey((x,y) => (x._1 + y._1, x._2 + y._2))
      * (33,(287,2))
      * rdd.mapValues(x=>(x,1)).reduceByKey((x,y) => (x._1 + y._1, x._2 + y._2)).mapValues(x => x._1/x._2)
      * (33,143.5)
      */
    val totalsByAge = rdd.mapValues(x => (x, 1)).reduceByKey((x,y) => (x._1 + y._1, x._2 + y._2))
    /**
      * _+_ means anything + anything e.g. (dog,2) + (dog,1). Now the key is dog the values are added like (dog,(2+1)).
      * So anything in the value of the key gets added up.
      */
    // val names = rdd.map(x=>(x,1)).reduceByKey(_+_)
    // So now we have tuples of (age, (totalFriends, totalInstances))
    // To compute the average we divide totalFriends / totalInstances for each age.
    val averagesByAge = totalsByAge.mapValues(x => x._1 / x._2)

    // Collect the results from the RDD (This kicks off computing the DAG and actually executes the job)
    val results = averagesByAge.collect()
    //val nameResults = names.collect()

    // Sort and print the final results.
    results.sorted.foreach(println)
    //nameResults.sorted.foreach(println)
  }

}
