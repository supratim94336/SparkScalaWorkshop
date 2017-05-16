package com.supra.scala

import java.nio.charset.CodingErrorAction
import org.apache.log4j._
import org.apache.spark._

import scala.io.{Codec, Source}
/**
  * Created by Supra on 30/12/2016.
  */
object PopMovies {

  def loadMovieNames(): Map[Int, String] = {
    /**
      * Creating maps in scala
      * var because it will be updated time and again (val only when it's immutable)
      */
    // In scala implicit parameters can also be changed like normal variables
    implicit val codec = Codec("UTF-8")

    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    var movieNames: Map[Int, String] = Map()

    // Reading movie names, it is immutable so val
    val movieNameReadings = Source.fromFile("../SparkScalaWorkshop/data/ml-100k/u.item").getLines()

    for(singleLine <- movieNameReadings){

      var fields = singleLine.split('|')

      if(fields.length > 1) {
        // movieID -> movieName
        movieNames+= (fields(0).toInt -> fields(1))
      }
    }

    movieNames
  }

  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine, named RatingsCounter
    val sc = new SparkContext("local[*]", "PopMovies")

    // Load up each line of the ratings data into an RDD
    val lines = sc.textFile("../SparkScalaWorkshop/data/ml-100k/u.data")

    // Convert each line to a string, split it out by tabs, and extract the second field.
    // (The file format is userID, movieID, rating, time stamp
    // movieID -> (movieID, 1)
    val movies = lines.map(x => (x.toString.split("\t")(1).toInt, 1))

    // A dictionary between name and movie_id
    val nameDict = sc.broadcast(loadMovieNames)

    /**
      * Basically to find out which movie_id is rated the highest times
      */
    // Count up how many times each value (rating) occurs: [ReducedByKey((value1,value2) => value1 + value2) for the same key]
    // 9, 1
    // 10, 2 ..
    val movieCounts = movies.reduceByKey(_+_)

    // Flip the resulting map of (Movies, Count) tuple
//    val flippedResults = movieCounts.map(x => (x._2, x._1))

    // More elegant way to swap keys and values
    // 2, 10
    // 1, 9 ...
    val flippedResults = movieCounts.map(_ swap)

    // Sort by key
    // 1, 9
    // 1, 10 ...
    // true for ascending and false for descending
    val sortedResults = flippedResults.sortByKey(false)

    // Match movieNames and results
    // Star Wars, 1
    // Alien, 1
    val sortedMoviesWithNames = sortedResults.map(x => (nameDict.value(x._2), x._1))

    val results = sortedMoviesWithNames.collect()

    // Print each result on its own line.
    results.foreach(println)

  }
}
