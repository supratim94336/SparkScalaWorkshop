package com.supra.scala

import org.apache.log4j._
import java.nio.charset.CodingErrorAction
import org.apache.spark.sql.SparkSession
import scala.io.Source
import scala.io.Codec

/**
  * Created by Supra on 15/01/2017.
  */
object PopularMoviesDataSets extends java.io.Serializable{

  def loadMovieNames(): Map[Int, String] = {
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    var movieNames: Map[Int, String] = Map()
    val lines = Source.fromFile("../ml-100k/u.item").getLines()
    for(line <- lines){
      var fields = line.split('|')
      if(fields.length > 1){
        movieNames+= (fields(0).toInt -> fields(1))
      }
    }
    return movieNames
  }

  // Case class so that we can get a column name for our movieID
  // Each DS takes a class a data format
  final case class Movie(movieId: Int)

  // Our main function where action happens
  def main(args: Array[String]): Unit ={
    Logger.getLogger("org").setLevel(Level.ERROR)
    // Spark session instead of SparkContext for Data-sets and Data-frames
    val spark = SparkSession
                .builder
                .appName("Pop_Movies")
                .master("local[*]")
                .getOrCreate()

    val lines = spark
                .sparkContext.textFile("../ml-100k/u.data")
                .map(x=>Movie(x.split("\t")(1).toInt))

    // Convert to data set
    import spark.implicits._
    val movieDS = lines.toDS()
    // Some SQL-style magic to sort all movies by popularity in one line! See the only column is named as movieID on the
    // run. count() creates a column named as count
    val topMovieIDS = movieDS
                      .groupBy("movieId")
                      .count()
                      .sort($"count".desc)
                      .cache()

    topMovieIDS.show()

    val top10 = topMovieIDS.take(10)
    val names = loadMovieNames()
    for(result <- top10){
      // Finding movie names from it's Integer ID and count part of result in dataset is result(1)
      println(names(result(0).asInstanceOf[Int]) + ": " + result(1))
    }

    spark.stop()

  }
}
